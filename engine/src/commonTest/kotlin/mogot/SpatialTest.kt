package mogot

import mogot.math.Matrix4f
import mogot.math.Vector3f
import kotlin.test.Test
import mogot.test.assertEquals
import mogot.test.eq

class SpatialTest {

    @Test
    fun transform() {
        val s = Spatial()
        s.scale.set(2f, 2f, 2f)
        s.lookTo(Vector3f(10f, 10f, 10f))
        s.position.set(3f, 0f, 3f)
        val mat = s.transform.getTranslation(Vector3f())
        assertEquals(3f, mat.x)
        assertEquals(0f, mat.y)
        assertEquals(3f, mat.z)
    }

    @Test
    fun globalTransformNoParent() {
        val s = Spatial()
        s.position.set(3f, 0f, 3f)
        val mat = Matrix4f()
        s.localToGlobalMatrix(mat)
        assertEquals(3f, mat.m30)
        assertEquals(0f, mat.m31)
        assertEquals(3f, mat.m32)
    }

    @Test
    fun globalTransformWithParent() {
        val root = Spatial()
        root.position.set(-1f, -2f, -3f)
        val s = Spatial()
        s.parent = root
        s.position.set(3f, 0f, 3f)
        s.lookTo(Vector3f(10f, 10f, 10f))

        val mat = Matrix4f()
        s.localToGlobalMatrix(mat)
        assertEquals(2f, mat.m30)
        assertEquals(-2f, mat.m31)
        assertEquals(0f, mat.m32)
    }

    @Test
    fun globalToLocal() {
        val s = Spatial()
        s.position.set(0f, 1f, 0f)
        s.globalToLocal(Vector3f(5f, 5f, 5f), Vector3f()).also {
            it.x.eq(5f)
            it.y.eq(4f)
            it.z.eq(5f)
        }

        s.globalToLocal(Vector3f(s.position), Vector3f()).also {
            it.x.eq(0f)
            it.y.eq(0f)
            it.z.eq(0f)
        }
    }

    @Test
    fun localToGlobal() {
        val s = Spatial()
        s.position.set(0f, 1f, 0f)
        s.localToGlobal(Vector3f(5f, 5f, 5f), Vector3f()).also {
            it.x.eq(5f)
            it.y.eq(6f)
            it.z.eq(5f)
        }

        s.localToGlobal(Vector3f(0f, 0f, 0f), Vector3f()).also {
            it.x.eq(s.position.x)
            it.y.eq(s.position.y)
            it.z.eq(s.position.z)
        }
    }
}