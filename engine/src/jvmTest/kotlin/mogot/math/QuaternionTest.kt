package mogot.math

import mogot.eq
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


        j.eqO(o)
    }
}

fun Quaternionf.eqO(q:OQuaternionf){
    x.eq(q.x,0.01f)
    y.eq(q.y,0.01f)
    z.eq(q.z,0.01f)
    w.eq(q.w,0.01f)
}