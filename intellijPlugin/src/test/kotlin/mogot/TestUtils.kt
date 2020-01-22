package mogot

import org.junit.Assert
import kotlin.math.abs
import kotlin.test.assertEquals

fun Float.eq(expected: Float, delta: Float = 0f): Float {
    Assert.assertEquals(expected, this, delta)
    return this
}

fun <T : Any> T.eq(value: T): T {
    assertEquals(value, this)
    return this
}