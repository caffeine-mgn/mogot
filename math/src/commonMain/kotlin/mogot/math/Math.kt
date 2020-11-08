package mogot.math

import kotlin.math.PI
import kotlin.math.acos

expect object Math {
    val PI: Double

    fun sin(value: Double): Double
    fun cos(value: Double): Double
    fun toRadians(value: Double): Double
    fun toDegrees(value: Double): Double
    fun fma(a: Float, b: Float, c: Float): Float
}


fun safeAcos(v: Double): Double {
    return if (v < -1.0) PI else if (v > +1.0) 0.0 else acos(v)
}

fun safeAcos(v: Float): Float {
    return if (v < -1.0f) PIf else if (v > +1.0) 0.0f else acos(v)
}

fun isPowerOfTwo(n: Int): Boolean {
    return n > 0 && n and n - 1 == 0
}

fun nextPowerOfTwo(n: Int): Int {
    var out = n
    out--
    out = out or (out shr 1)
    out = out or (out shr 2)
    out = out or (out shr 4)
    out = out or (out shr 8)
    out = out or (out shr 16)
    out++
    return out
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
fun Float.lerp(to: Float, cof: Float) = this + cof * (to - this)