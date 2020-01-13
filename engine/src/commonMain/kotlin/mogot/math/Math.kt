package mogot.math

import kotlin.jvm.JvmName

expect object Math {
    val PI: Double

    fun sin(value: Double): Double
    fun cos(value: Double): Double
    fun toRadians(value: Double): Double
    fun toDegrees(value: Double): Double
    fun fma(a: Float, b: Float, c: Float): Float
}


private const val DEGREES_TO_RADIANS = 0.017453292519943295
private const val DEGREES_TO_RADIANSf = 0.017453292519943295f
private const val RADIANS_TO_DEGREES = 57.29577951308232
private const val RADIANS_TO_DEGREESf = 57.29577951308232f
fun toRadians(value: Double): Double = value * DEGREES_TO_RADIANS
fun toRadians(value: Float): Float = value * DEGREES_TO_RADIANSf
fun toDegrees(value: Double): Double = value * RADIANS_TO_DEGREES
fun toDegrees(value: Float): Float = value * RADIANS_TO_DEGREESf
inline val Float.isPositive
    get() = this > 0f
val MATRIX4_ONE: Matrix4fc = Matrix4f().identity()