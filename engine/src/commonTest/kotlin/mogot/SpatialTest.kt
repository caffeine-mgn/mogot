package mogot

import mogot.math.Matrix4f
import mogot.math.PIf
import mogot.math.Vector3f
import kotlin.test.Test
import mogot.test.assertEquals

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
        s.globalTransfrorm(mat)
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
        s.globalTransfrorm(mat)
        assertEquals(2f, mat.m30)
        assertEquals(-2f, mat.m31)
        assertEquals(0f, mat.m32)
    }
}