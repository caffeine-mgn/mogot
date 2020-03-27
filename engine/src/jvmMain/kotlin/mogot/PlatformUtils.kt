package mogot

actual object PlatformUtils {
    actual fun getSystemTimeMillis(): Long = System.currentTimeMillis()

    actual fun getSystemTimeNano(): Long = System.nanoTime()
}