@file:JvmName("MathJava")
package mogot.math

import java.lang.Math as JMath

actual object Math {
    actual val PI: Double
        get() = JMath.PI

    actual fun sin(value: Double): Double = JMath.sin(value)

    actual fun cos(value: Double): Double = JMath.cos(value)

    actual fun toRadians(value: Double): Double = JMath.toRadians(value)
    actual fun toDegrees(value: Double): Double = JMath.toDegrees(value)
    actual fun fma(a: Float, b: Float, c: Float): Float {
        return java.lang.Math.fma(a, b, c)
        //a * b + c
    }

}