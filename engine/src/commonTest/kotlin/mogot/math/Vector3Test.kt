package mogot.math

import mogot.eq
import kotlin.test.Test

class Vector3Test {

    @Test
    fun plusAssignTest() {
        val zero = Vector3f()

        zero += Vector3f(5f, 6f, 7f)
        zero.x.eq(5f)
        zero.y.eq(6f)
        zero.z.eq(7f)

        zero += Vector3f(1f, 2f, 3f)

        zero.x.eq(6f)
        zero.y.eq(8f)
        zero.z.eq(10f)
    }
}