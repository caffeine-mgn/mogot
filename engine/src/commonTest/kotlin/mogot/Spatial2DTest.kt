package mogot

import mogot.math.Vector2f
import mogot.math.Vector2fc
import kotlin.test.Test

class Spatial2DTest {

    @Test
    fun transform() {
        val e = mockEngine()
        val s = Spatial2D(e)

        s.transform.getTranslation().also {
            it.x.eq(0f)
            it.y.eq(0f)
            it.z.eq(0f)
        }

        s.position.set(10f, 10f)

        s.transform.getTranslation().also {
            it.x.eq(10f)
            it.y.eq(10f)
            it.z.eq(0f)
        }
    }

    @Test
    fun globalToLocal() {
        val e = mockEngine()
        val s = Spatial2D(e)
        s.globalToLocal(Vector2f()).also {
            it.x.eq(0f)
            it.y.eq(0f)
        }

        s.position.set(10f, 10f)
        s.globalToLocal(Vector2f()).also {
            it.x.eq(-10f)
            it.y.eq(-10f)
        }
        s.globalToLocal(Vector2f(10f, 10f)).also {
            it.x.eq(0f)
            it.y.eq(0f)
        }
    }

    @Test
    fun localToGlobal() {
        val e = mockEngine()
        val s = Spatial2D(e)

        s.position.set(10f, 10f)
        s.localToGlobal(Vector2f(0f, 0f)).also {
            it.x.eq(10f)
            it.y.eq(10f)
        }

        val s2 = Spatial2D(e)
        s2.parent = s
        s2.position.set(15f, 15f)
        s2.localToGlobal(Vector2f()).also {
            it.x.eq(25f)
            it.y.eq(25f)
        }
    }
}