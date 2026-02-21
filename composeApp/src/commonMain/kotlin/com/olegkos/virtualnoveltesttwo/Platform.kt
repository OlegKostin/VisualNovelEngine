package com.olegkos.virtualnoveltesttwo

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform