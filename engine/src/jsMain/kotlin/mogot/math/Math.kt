package mogot.math

actual object Math {
    actual val PI: Double
        get() = kotlin.math.PI

    actual fun sin(value: Double): Double = kotlin.math.sin(value)

    actual fun cos(value: Double): Double = kotlin.math.cos(value)

    actual fun toRadians(value: Double): Double = value * PI / 180.0

}