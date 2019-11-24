package mogot.math

import org.junit.Assert
import org.junit.Test
import org.joml.Matrix4f as OMatrix4f
import org.joml.Quaternionf as OQuaternionf


class Matrix4fTest {

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


        Matrix4f().identity().translate(pos) eq OMatrix4f().identity().translate(pos)

        val scale = Vector3f(1f, 1f, 1f)
        val q = Quaternionf()
        q.lookAlong(Vector3f(0f, 0f, 0f), Vec3f.UP)
        Matrix4f().identity()
                .translate(pos)
                .rotate(q)
                .scale(scale) eq
                OMatrix4f().identity()
                        .translate(pos)
                        .rotate(q.toO())
                        .scale(scale)
    }
}

fun Quaternionf.toO() = OQuaternionf(x, y, z, w)

infix fun Matrix4f.eq(other: OMatrix4f) {
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
}