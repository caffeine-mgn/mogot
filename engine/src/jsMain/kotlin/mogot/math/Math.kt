package mogot.math

actual object Math {
    actual val PI: Double
        get() = kotlin.math.PI
    private const val DEGREES_TO_RADIANS = 0.017453292519943295

    actual fun sin(value: Double): Double = kotlin.math.sin(value)

    actual fun cos(value: Double): Double = kotlin.math.cos(value)

    actual fun toRadians(value: Double): Double = value * DEGREES_TO_RADIANS
    actual fun fma(a: Float, b: Float, c: Float): Float = a * b + c
}