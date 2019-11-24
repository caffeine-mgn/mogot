package mogot.math

import java.lang.Math as JMath

actual object Math {
    actual val PI: Double
        get() = JMath.PI

    actual fun sin(value: Double): Double = JMath.sin(value)

    actual fun cos(value: Double): Double = JMath.cos(value)

    actual fun toRadians(value: Double): Double = JMath.toRadians(value)

}