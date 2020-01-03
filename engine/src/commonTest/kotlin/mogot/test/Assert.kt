package mogot.test

import kotlin.math.abs

fun assertEquals(expected: Float, actual: Float, delta: Float = 0f) {
    if (abs(actual - expected) > abs(delta))
        throw AssertionError("expected: $expected, but was: $actual")
}

fun Float.eq(expected: Float, delta: Float = 0f): Float {
    if (abs(this - expected) > abs(delta))
        throw AssertionError("expected: $expected, but was: $this")
    return this
}