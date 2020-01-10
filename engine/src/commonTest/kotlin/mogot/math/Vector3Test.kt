package mogot.math

import kotlin.test.Test
import mogot.test.assertEquals

class Vector3Test {

    @Test
    fun plusAssignTest() {
        val zero = Vector3f()

        zero += Vector3f(5f, 6f, 7f)
        assertEquals(5f, zero.x)
        assertEquals(6f, zero.y)
        assertEquals(7f, zero.z)

        zero += Vector3f(1f, 2f, 3f)

        assertEquals(6f, zero.x)
        assertEquals(8f, zero.y)
        assertEquals(10f, zero.z)
    }
}