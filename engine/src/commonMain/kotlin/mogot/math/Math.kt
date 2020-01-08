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
private const val RADIANS_TO_DEGREES = 57.29577951308232
fun toRadians(value: Double): Double = value * DEGREES_TO_RADIANS
fun toDegrees(value: Double): Double = value * RADIANS_TO_DEGREES
val MATRIX4_ONE: Matrix4fc = Matrix4f().identity()