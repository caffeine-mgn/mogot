package mogot.math

import mogot.test.eq
import org.junit.Test
import org.joml.Quaternionf as OQuaternionf
import org.joml.Vector3f as OVector3f

class QuaternionTest {
    @Test
    fun lookTo() {
        val o = OQuaternionf()
        o.lookAlong(OVector3f(0f, 0f, 1f), Vector3fc.UP.toO())
        val v = Quaternionf()

        val j = Quaternionf()
        j.lookAlong(Vector3f(0f, 0f, 1f), Vector3fc.UP)


        j.eq(o)
    }
}

fun Quaternionf.eq(q:OQuaternionf){
    x.eq(q.x)
    y.eq(q.y)
    z.eq(q.z)
    w.eq(q.w)
}