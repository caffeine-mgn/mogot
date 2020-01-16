package mogot

import mogot.math.*
import kotlin.test.Test

class SpatialTest {

    @Test
    fun localToGlobalMatrix() {
        val root = Spatial()
        val child = Spatial()
        child.parent = root
        root.position.set(3f, 3f, 3f)
        child.position.set(2f, 2f, 2f)
        child.localToGlobalMatrix(Matrix4f()).getTranslation(Vector3f()).also {
            it.x.eq(5f)
            it.y.eq(5f)
            it.z.eq(5f)
        }
    }

    @Test
    fun globalToLocalMatrix() {
        val root = Spatial()
        val child = Spatial()
        child.parent = root
        root.position.set(3f, 3f, 3f)
        child.position.set(2f, 2f, 2f)
        child.globalToLocalMatrix(Matrix4f()).getTranslation(Vector3f()).also {
            it.x.eq(-5f)
            it.y.eq(-5f)
            it.z.eq(-5f)
        }
    }

    @Test
    fun setGlobalTransform1() {
        val s = Spatial()
        s.position.set(5f, 5f, 5f)
        val globalMat = s.localToGlobalMatrix(Matrix4f())
        val root = Spatial()
        root.position.set(3f, 3f, 3f)
        s.parent = root
        s.setGlobalTransform(globalMat)

        s.localToGlobal(Vector3fc.ZERO, Vector3f()).also {
            it.x.eq(5f)
            it.y.eq(5f)
            it.z.eq(5f)
        }

        s.position.also {
            it.x.eq(2f)
            it.y.eq(2f)
            it.z.eq(2f)
        }
    }

    @Test
    fun setGlobalTransform() {
        val root = Spatial()
        val child = Spatial()
        root.position.set(3f, 0f, 0f)
        root.quaternion.identity()
        root.quaternion.setRotation(0f, toRadians(90f), 0f)
        child.parent = root
        child.setGlobalTransform(Matrix4f().translate(4f, 0f, 0f).scale(1f, 1f, 1f))

//        RotationVector(child.quaternion).also {
//            it.x.eq(0f)
//            it.y.eq(0f)
//            it.z.eq(0f)
//        }

        child.position.also {
            it.x.eq(0f, 0.01f)
            it.y.eq(0f, 0.01f)
            it.z.eq(1f, 0.01f)
        }

        child.localToGlobal(Vector3f(), Vector3f()).also {
            it.x.eq(4f, 0.01f)
            it.y.eq(0f, 0.01f)
            it.z.eq(0f, 0.01f)
        }
//        child.globalToLocal(Vector3f(4f, 0f, 0f), Vector3f()).also {
//            it.x.eq(0f, 0.01f)
//            it.y.eq(0f, 0.01f)
//            it.z.eq(0f, 0.01f)
//        }
    }

    @Test
    fun logalAndGlobal() {
        val root = Spatial()
        val child = Spatial()
        root.position.set(3f, 0f, 0f)
        root.quaternion.identity()
        root.quaternion.setRotation(0f, toRadians(90f), 0f)
        child.parent = root
        child.position.set(0f, 0f, 1f)

        child.localToGlobal(Vector3f(), Vector3f()).also {
            it.x.eq(4f, 0.01f)
            it.y.eq(0f, 0.01f)
            it.z.eq(0f, 0.01f)
        }
        child.globalToLocal(Vector3f(4f, 0f, 0f), Vector3f()).also {
            it.x.eq(0f, 0.01f)
            it.y.eq(0f, 0.01f)
            it.z.eq(0f, 0.01f)
        }
    }

    @Test
    fun lookToTest() {
        val s = Spatial()
        s.quaternion.lookAlong(Vector3f(0f, 0f, 1f), Vector3fc.UP)
        s.quaternion.mul(Vector3f(0f, 0f, 1f), Vector3f()).also {
            it.x.eq(0f)
            it.y.eq(0f)
            it.z.eq(-1f)
        }
        s.lookTo(Vector3f(0f, 0f, 1f))
        s.quaternion.yaw.eq(0f)
        s.quaternion.pitch.eq(0f)
        s.quaternion.roll.eq(0f)
    }

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
        val c = Spatial()
        c.position.set(1f, 2f, 3f)
        val s = Spatial()
        s.parent = c
        s.position.set(0f, 1f, 0f)
        s.localToGlobal(Vector3f(5f, 5f, 5f), Vector3f()).also {
            it.x.eq(5f + c.position.x)
            it.y.eq(6f + c.position.y)
            it.z.eq(5f + c.position.z)
        }

        s.localToGlobal(Vector3f(0f, 0f, 0f), Vector3f()).also {
            it.x.eq(s.position.x + c.position.x)
            it.y.eq(s.position.y + c.position.y)
            it.z.eq(s.position.z + c.position.z)
        }
    }

    @Test
    fun test() {
        val vv = Spatial()
        vv.position.set(3f, 0f, 0f)
        val q = Quaternionf()
        q.setRotation(0f, toRadians(90f), 0f)
        val mat = vv.localToGlobalMatrix(Matrix4f())
        mat.getTranslation(Vector3f()).also {
            it.x.eq(3f)
            it.y.eq(0f)
            it.z.eq(0f)
        }
        val rotMat = Matrix4f().rotate(q)
        mat.translate(-2f,0f,0f)

        mat.mul(rotMat, mat)
        mat.translate(2f,0f,0f)
        println("POS=${mat.getTranslation(Vector3f())}")

//        mat.translate(2f,0f,0f)
        mat.getTranslation(Vector3f()).also {
            it.x.eq(2f)
            it.y.eq(0f)
            it.z.eq(-1f, 0.01f)
        }
    }
}