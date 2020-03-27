package mogot

actual object CurrentTime {
    actual fun getMillis(): Long = System.currentTimeMillis()

    actual fun getNano(): Long = System.nanoTime()
}