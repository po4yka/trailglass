package com.po4yka.trailglass

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
