package mogot

import kotlin.math.abs
import kotlin.test.assertEquals


fun <T : Any> T.eq(value: T): T {
    assertEquals(value, this)
    return this
}

fun assertEquals(expected: Float, actual: Float, delta: Float = 0f) {
    if (abs(actual - expected) > abs(delta))
        throw AssertionError("expected: $expected, but was: $actual")
}

fun Float.eq(expected: Float, delta: Float = 0f): Float {
    if (abs(this - expected) > abs(delta))
        throw AssertionError("expected: $expected, but was: $this")
    return this
}