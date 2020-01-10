package mogot.math

import mogot.Camera
import org.junit.Assert
import org.junit.Test
import org.joml.Matrix4f as OMatrix4f
import org.joml.Quaternionf as OQuaternionf
import org.joml.Vector3f as OVector3f

fun Vector3fc.toO() = OVector3f(x, y, z)


class Matrix4fTest {

    @Test
    fun test2() {
        val cam = Camera()
        cam.resize(200, 100)
        cam.position.set(0f, 1f, 1f)
        cam.lookTo(Vector3f(0f, 0f, -1f))

        val p = OMatrix4f().setPerspective(
                Math.toRadians(cam.fieldOfView.toDouble()).toFloat(),
                cam.width.toFloat() / cam.height.toFloat(),
                cam.near,
                cam.far, false)
        cam.projectionMatrix.eq(p)
        cam.projectionMatrix.invert(Matrix4f()).eq(p.invert(OMatrix4f()))

        val tr = OMatrix4f().translationRotateScale(cam.position.toO(), cam.quaternion.toO(), cam.scale.toO())
        cam.transform.eq(tr)
        val pv = p.mul(tr, OMatrix4f())
        val mogotPV = cam.projectionMatrix.mul(cam.transform, Matrix4f())
        mogotPV.eq(pv)

        val pvInvert = pv.invert(OMatrix4f())
        val mogotPVInvert = mogotPV.invert(Matrix4f())
        mogotPVInvert.eq(pvInvert)

        val pos = OVector3f()
        val dir = OVector3f()
        pv.unprojectRay(
                cam.width / 2f,
                cam.height / 2f,
                intArrayOf(0, 0, cam.width, cam.height),
                pos,
                dir
        )
        val ray = MutableRay()
        cam.screenPointToRay(cam.width / 2, cam.height / 2, ray)
        ray.position.eq(pos,0.01f)
        ray.direction.eq(dir,0.01f)
    }

    @Test
    fun test() {
        Matrix4f().eq(OMatrix4f())
        Matrix4f().identity().eq(OMatrix4f().identity())



        Matrix4f().identity().eq(OMatrix4f().identity())

        val width = 800
        val height = 600
        Matrix4f().identity().perspective(Math.toRadians(45.0).toFloat(), width.toFloat() / height.toFloat(), 0.1f, 1000f) eq
                OMatrix4f().identity().perspective(Math.toRadians(45.0).toFloat(), width.toFloat() / height.toFloat(), 0.1f, 1000f)


        val pos = Vector3f(3f, 3f, 3f)


        Matrix4f().identity().translate(pos) eq OMatrix4f().identity().translate(pos.toO())

        val scale = Vector3f(1f, 1f, 1f)
        val q = Quaternionf()
        q.lookAlong(Vector3f(0f, 0f, 0f), Vector3fc.UP)
        Matrix4f().identity()
                .translate(pos)
                .rotate(q)
                .scale(scale) eq
                OMatrix4f().identity()
                        .translate(pos.toO())
                        .rotate(q.toO())
                        .scale(scale.toO())
    }
}

fun Quaternionfc.toO() = OQuaternionf(x, y, z, w)

fun Vector3fc.eq(other: OVector3f, delta: Float = 0f) {
    Assert.assertEquals(x, other.x(), delta)
    Assert.assertEquals(y, other.y(), delta)
    Assert.assertEquals(z, other.z(), delta)
}

infix fun Matrix4fc.eq(other: OMatrix4f): Matrix4fc {
    try {
        Assert.assertEquals(m00, other.m00())
        Assert.assertEquals(m01, other.m01())
        Assert.assertEquals(m02, other.m02())
        Assert.assertEquals(m03, other.m03())



        Assert.assertEquals(m10, other.m10())
        Assert.assertEquals(m11, other.m11())
        Assert.assertEquals(m12, other.m12())
        Assert.assertEquals(m13, other.m13())

        Assert.assertEquals(m20, other.m20())
        Assert.assertEquals(m21, other.m21())
        Assert.assertEquals(m22, other.m22())
        Assert.assertEquals(m23, other.m23())

        Assert.assertEquals(m30, other.m30())
        Assert.assertEquals(m31, other.m31())
        Assert.assertEquals(m32, other.m32())
        Assert.assertEquals(m33, other.m33())
    } catch (e: Throwable) {

        println("MogotMatrix:\n$this")

        println("jomlMatrix:\n$other")

        throw e
    }
    return this
}