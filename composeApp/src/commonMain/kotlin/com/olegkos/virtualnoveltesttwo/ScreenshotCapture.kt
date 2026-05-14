package com.olegkos.virtualnoveltesttwo

/** PNG bytes окна приложения; на платформах без захвата — null. */
expect suspend fun captureScreenshotPngBytes(): ByteArray?
