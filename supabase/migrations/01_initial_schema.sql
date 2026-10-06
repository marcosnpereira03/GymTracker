-- ==============================================================================
-- SCHEMA DEFINITION: Gym Tracker
-- Plataforma: PostgreSQL / Supabase
-- Multi-usuario con Row Level Security (RLS)
-- ==============================================================================

-- 1. Extensiones necesarias
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- 2. Tabla: Pesajes
-- Registra el peso corporal en una fecha determinada para seguimiento de composición/peso
CREATE TABLE IF NOT EXISTS public.pesajes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    fecha DATE NOT NULL DEFAULT CURRENT_DATE,
    peso_kg NUMERIC(5, 2) NOT NULL CHECK (peso_kg > 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- 3. Tabla: Ejercicios
-- Catálogo maestro de ejercicios por usuario
CREATE TABLE IF NOT EXISTS public.ejercicios (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    nombre VARCHAR(100) NOT NULL,
    grupo_muscular VARCHAR(50) NOT NULL,
    equipamiento VARCHAR(50),
    notas TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT unique_user_ejercicio UNIQUE (user_id, nombre)
);

-- 4. Tabla: Entrenamientos
-- Sesión diaria de entrenamiento
CREATE TABLE IF NOT EXISTS public.entrenamientos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    fecha DATE NOT NULL DEFAULT CURRENT_DATE,
    nombre_sesion VARCHAR(80),
    observaciones TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- 5. Tabla: Series Realizadas
-- Registro granular de cada serie individual
CREATE TABLE IF NOT EXISTS public.series_realizadas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    entrenamiento_id UUID NOT NULL REFERENCES public.entrenamientos(id) ON DELETE CASCADE,
    ejercicio_id UUID NOT NULL REFERENCES public.ejercicios(id) ON DELETE RESTRICT,
    numero_serie INTEGER NOT NULL CHECK (numero_serie > 0),
    peso_kg NUMERIC(5, 2) NOT NULL CHECK (peso_kg >= 0),
    repeticiones INTEGER NOT NULL CHECK (repeticiones > 0),
    rir INTEGER NOT NULL CHECK (rir >= 0 AND rir <= 10),
    observaciones TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ==============================================================================
-- ÍNDICES DE RENDIMIENTO
-- ==============================================================================

CREATE INDEX IF NOT EXISTS idx_series_ejercicio_created 
    ON public.series_realizadas (ejercicio_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_series_entrenamiento 
    ON public.series_realizadas (entrenamiento_id);

CREATE INDEX IF NOT EXISTS idx_entrenamientos_user_fecha 
    ON public.entrenamientos (user_id, fecha DESC);

CREATE INDEX IF NOT EXISTS idx_pesajes_user_fecha 
    ON public.pesajes (user_id, fecha DESC);

CREATE INDEX IF NOT EXISTS idx_ejercicios_user_grupo 
    ON public.ejercicios (user_id, grupo_muscular);

-- ==============================================================================
-- ROW LEVEL SECURITY (RLS)
-- Cada usuario sólo puede consultar, insertar, modificar y eliminar sus propios datos
-- ==============================================================================

ALTER TABLE public.pesajes ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.ejercicios ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.entrenamientos ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.series_realizadas ENABLE ROW LEVEL SECURITY;

-- Políticas para Pesajes
DROP POLICY IF EXISTS "Users can manage their own pesajes" ON public.pesajes;
CREATE POLICY "Users can manage their own pesajes"
    ON public.pesajes FOR ALL
    USING (auth.uid() = user_id)
    WITH CHECK (auth.uid() = user_id);

-- Políticas para Ejercicios
DROP POLICY IF EXISTS "Users can manage their own ejercicios" ON public.ejercicios;
CREATE POLICY "Users can manage their own ejercicios"
    ON public.ejercicios FOR ALL
    USING (auth.uid() = user_id)
    WITH CHECK (auth.uid() = user_id);

-- Políticas para Entrenamientos
DROP POLICY IF EXISTS "Users can manage their own entrenamientos" ON public.entrenamientos;
CREATE POLICY "Users can manage their own entrenamientos"
    ON public.entrenamientos FOR ALL
    USING (auth.uid() = user_id)
    WITH CHECK (auth.uid() = user_id);

-- Políticas para Series Realizadas
DROP POLICY IF EXISTS "Users can manage their own series" ON public.series_realizadas;
CREATE POLICY "Users can manage their own series"
    ON public.series_realizadas FOR ALL
    USING (auth.uid() = user_id)
    WITH CHECK (auth.uid() = user_id);
