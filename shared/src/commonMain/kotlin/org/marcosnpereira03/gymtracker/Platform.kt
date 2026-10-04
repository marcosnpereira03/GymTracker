package org.marcosnpereira03.gymtracker

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform