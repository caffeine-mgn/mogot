package mogot.math

interface Vector4fc {
    val x: Float
    val y: Float
    val z: Float
    val w: Float
}



fun Vector4f.mul(mat: Matrix4fc): Vector4f = mul(mat, this)

fun Vector4fc.mul(mat: Matrix4fc, dest: Vector4f): Vector4f =
        if (mat.properties and PROPERTY_AFFINE != 0)
            mulAffine(mat, dest)
        else
            mulGeneric(mat, dest)

private fun Vector4fc.mulAffine(mat: Matrix4fc, dest: Vector4f): Vector4f {
    val rx: Float = mat.m00 * x + mat.m10 * y + mat.m20 * z + mat.m30 * w
    val ry: Float = mat.m01 * x + mat.m11 * y + mat.m21 * z + mat.m31 * w
    val rz: Float = mat.m02 * x + mat.m12 * y + mat.m22 * z + mat.m32 * w
    dest.x = rx
    dest.y = ry
    dest.z = rz
    dest.w = w
    return dest
}

private fun Vector4fc.mulGeneric(mat: Matrix4fc, dest: Vector4f): Vector4f {
    val rx: Float = mat.m00 * x + mat.m10 * y + mat.m20 * z + mat.m30 * w
    val ry: Float = mat.m01 * x + mat.m11 * y + mat.m21 * z + mat.m31 * w
    val rz: Float = mat.m02 * x + mat.m12 * y + mat.m22 * z + mat.m32 * w
    val rw: Float = mat.m03 * x + mat.m13 * y + mat.m23 * z + mat.m33 * w
    dest.x = rx
    dest.y = ry
    dest.z = rz
    dest.w = rw
    return dest
}

open class Vector4f(override var x: Float = 0f, override var y: Float = 0f, override var z: Float = 0f, override var w: Float = 0f) : Vector4fc {
    fun set(x: Float, y: Float, z: Float, w: Float) {
        this.x = x
        this.y = y
        this.z = z
        this.w = w
    }

    override fun toString(): String =
            "Vec4f($x $y $y $w)"
}

val Vector4fc.isNaN
    get() = x.isNaN() || y.isNaN() || z.isNaN() || w.isNaN()