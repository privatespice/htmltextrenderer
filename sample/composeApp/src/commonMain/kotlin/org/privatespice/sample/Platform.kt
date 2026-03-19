package org.privatespice.sample

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform