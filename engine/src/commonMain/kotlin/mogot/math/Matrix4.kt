@file:JvmName("MathCommonKt")

package mogot.math


import kotlin.jvm.JvmName
import kotlin.math.PI

interface Matrix4fc {
    fun getTranslation(dest: Vector3f): Vector3f
    val m00: Float
    val m01: Float
    val m02: Float
    val m03: Float
    val m10: Float
    val m11: Float
    val m12: Float
    val m13: Float
    val m20: Float
    val m21: Float
    val m22: Float
    val m23: Float
    val m30: Float
    val m31: Float
    val m32: Float
    val m33: Float

    val properties: Int
    fun translate(x: Float, y: Float, z: Float, dest: Matrix4f): Matrix4f {
//        if (properties and PROPERTY_IDENTITY != 0)
//            return dest.translation(x, y, z)

        dest.set(this)
        dest.m30 = (m00 * x + m10 * y + m20 * z + m30)
        dest.m31 = (m01 * x + m11 * y + m21 * z + m31)
        dest.m32 = (m02 * x + m12 * y + m22 * z + m32)
        dest.m33 = (m03 * x + m13 * y + m23 * z + m33)
        dest.properties = (properties and (PROPERTY_PERSPECTIVE or PROPERTY_IDENTITY).inv())
        return dest
    }

    fun rotate(other: Quaternionfc, dest: Matrix4f): Matrix4f {
        dest.set(this)
        return dest.rotate(other)
    }

    fun translate(vector: Vector3fc, dest: Matrix4f) = translate(vector.x, vector.y, vector.z, dest)
    fun scale(x: Float, y: Float, z: Float, dest: Matrix4f): Matrix4f {
        return if (properties and PROPERTY_IDENTITY != 0)
            dest.scaling(x, y, z)
        else {
            dest.m00 = (m00 * x)
            dest.m01 = (m01 * x)
            dest.m02 = (m02 * x)
            dest.m03 = (m03 * x)
            dest.m10 = (m10 * y)
            dest.m11 = (m11 * y)
            dest.m12 = (m12 * y)
            dest.m13 = (m13 * y)
            dest.m20 = (m20 * z)
            dest.m21 = (m21 * z)
            dest.m22 = (m22 * z)
            dest.m23 = (m23 * z)
            dest.m30 = (m30)
            dest.m31 = (m31)
            dest.m32 = (m32)
            dest.m33 = (m33)
            val one = kotlin.math.abs(x) == 1.0f && kotlin.math.abs(y) == 1.0f && kotlin.math.abs(z) == 1.0f
            dest.properties = (properties and (PROPERTY_PERSPECTIVE or PROPERTY_IDENTITY or PROPERTY_TRANSLATION
                    or if (one) 0 else PROPERTY_ORTHONORMAL).inv())
            return dest
        }
    }

    fun perspective(fovy: Float, aspect: Float, zNear: Float, zFar: Float, zZeroToOne: Boolean, dest: Matrix4f): Matrix4f {
        dest.set(this)
        dest.perspective(fovy, aspect, zNear, zFar, zZeroToOne, dest)
        return dest
    }

    fun perspective(fovy: Float, aspect: Float, zNear: Float, zFar: Float, dest: Matrix4f): Matrix4f =
            perspective(fovy, aspect, zNear, zFar, false, dest);
}


var PROPERTY_IDENTITY = 1 shl 2
var PROPERTY_AFFINE = 1 shl 1
var PROPERTY_TRANSLATION = 1 shl 3
var PROPERTY_ORTHONORMAL = 1 shl 4
var PROPERTY_PERSPECTIVE = 1 shl 0

class Matrix4f : Matrix4fc {
    override var properties = PROPERTY_IDENTITY or PROPERTY_AFFINE or PROPERTY_TRANSLATION or PROPERTY_ORTHONORMAL
    //internal val data = Float32Array(16)
    override var m00 = 1f
    override var m01 = 0f
    override var m02 = 0f
    override var m03 = 0f
    override var m10 = 0f
    override var m11 = 1f
    override var m12 = 0f
    override var m13 = 0f
    override var m20 = 0f
    override var m21 = 0f
    override var m22 = 1f
    override var m23 = 0f
    override var m30 = 0f
    override var m31 = 0f
    override var m32 = 0f
    override var m33 = 1f

    fun identity(): Matrix4f {
        if (properties and PROPERTY_IDENTITY != 0)
            return this
        m00 = 1.0f
        m01 = 0.0f
        m02 = 0.0f
        m03 = 0.0f
        m10 = 0.0f
        m11 = 1.0f
        m12 = 0.0f
        m13 = 0.0f
        m20 = 0.0f
        m21 = 0.0f
        m22 = 1.0f
        m23 = 0.0f
        m30 = 0.0f
        m31 = 0.0f
        m32 = 0.0f
        m33 = 1.0f
        properties = PROPERTY_IDENTITY or PROPERTY_AFFINE or PROPERTY_TRANSLATION or PROPERTY_ORTHONORMAL
        return this
    }

    override fun toString(): String =
            "$m00  $m01  $m02 $m03\n" +
                    "$m10  $m11  $m12 $m13\n" +
                    "$m20  $m21  $m22 $m23\n" +
                    "$m30  $m31  $m32 $m33\n"

    fun set(other: Matrix4fc): Matrix4f {
        m00 = other.m00
        m01 = other.m01
        m02 = other.m02
        m03 = other.m03

        m10 = other.m10
        m11 = other.m11
        m12 = other.m12
        m13 = other.m13

        m20 = other.m20
        m21 = other.m21
        m22 = other.m22
        m23 = other.m23

        m30 = other.m30
        m31 = other.m31
        m32 = other.m32
        m33 = other.m33
        properties = other.properties
        return this
    }

    fun translation(x: Float, y: Float, z: Float): Matrix4f = translate(x, y, z, this)

    fun translate(vector: Vector3fc): Matrix4f = translation(vector.x, vector.y, vector.z)

    override fun rotate(other: Quaternionfc, dest: Matrix4f): Matrix4f {
        val quat = other
        if (properties and PROPERTY_IDENTITY != 0)
            return dest.rotation(quat) else
            if (properties and PROPERTY_TRANSLATION != 0)
                return rotateTranslation(quat, dest)
            else
                if (properties and PROPERTY_AFFINE != 0)
                    return rotateAffine(quat, dest)
        return rotateGeneric(quat, dest)
    }

    fun rotateAffine(quat: Quaternionfc, dest: Matrix4f): Matrix4f {
        val w2: Float = quat.w * quat.w
        val x2: Float = quat.x * quat.x
        val y2: Float = quat.y * quat.y
        val z2: Float = quat.z * quat.z
        val zw: Float = quat.z * quat.w
        val dzw = zw + zw
        val xy: Float = quat.x * quat.y
        val dxy = xy + xy
        val xz: Float = quat.x * quat.z
        val dxz = xz + xz
        val yw: Float = quat.y * quat.w
        val dyw = yw + yw
        val yz: Float = quat.y * quat.z
        val dyz = yz + yz
        val xw: Float = quat.x * quat.w
        val dxw = xw + xw
        val rm00 = w2 + x2 - z2 - y2
        val rm01 = dxy + dzw
        val rm02 = dxz - dyw
        val rm10 = -dzw + dxy
        val rm11 = y2 - z2 + w2 - x2
        val rm12 = dyz + dxw
        val rm20 = dyw + dxz
        val rm21 = dyz - dxw
        val rm22 = z2 - y2 - x2 + w2
        val nm00 = m00 * rm00 + m10 * rm01 + m20 * rm02
        val nm01 = m01 * rm00 + m11 * rm01 + m21 * rm02
        val nm02 = m02 * rm00 + m12 * rm01 + m22 * rm02
        val nm10 = m00 * rm10 + m10 * rm11 + m20 * rm12
        val nm11 = m01 * rm10 + m11 * rm11 + m21 * rm12
        val nm12 = m02 * rm10 + m12 * rm11 + m22 * rm12
        dest.m20 = (m00 * rm20 + m10 * rm21 + m20 * rm22)
        dest.m21 = (m01 * rm20 + m11 * rm21 + m21 * rm22)
        dest.m22 = (m02 * rm20 + m12 * rm21 + m22 * rm22)
        dest.m23 = (0.0f)
        dest.m00 = (nm00)
        dest.m01 = (nm01)
        dest.m02 = (nm02)
        dest.m03 = (0.0f)
        dest.m10 = (nm10)
        dest.m11 = (nm11)
        dest.m12 = (nm12)
        dest.m13 = (0.0f)
        dest.m30 = (m30)
        dest.m31 = (m31)
        dest.m32 = (m32)
        dest.m33 = (m33)

        dest.properties = (properties and (PROPERTY_PERSPECTIVE or PROPERTY_IDENTITY or PROPERTY_TRANSLATION).inv())
        return dest
    }

    fun rotateAffine(ang: Float, x: Float, y: Float, z: Float, dest: Matrix4f): Matrix4f {
        val s = kotlin.math.sin(ang.toDouble()) as Float
        val c = cosFromSin(s.toDouble(), ang.toDouble()) as Float
        val C = 1.0f - c
        val xx = x * x
        val xy = x * y
        val xz = x * z
        val yy = y * y
        val yz = y * z
        val zz = z * z
        val rm00 = xx * C + c
        val rm01 = xy * C + z * s
        val rm02 = xz * C - y * s
        val rm10 = xy * C - z * s
        val rm11 = yy * C + c
        val rm12 = yz * C + x * s
        val rm20 = xz * C + y * s
        val rm21 = yz * C - x * s
        val rm22 = zz * C + c
        // add temporaries for dependent values
        val nm00 = m00 * rm00 + m10 * rm01 + m20 * rm02
        val nm01 = m01 * rm00 + m11 * rm01 + m21 * rm02
        val nm02 = m02 * rm00 + m12 * rm01 + m22 * rm02
        val nm10 = m00 * rm10 + m10 * rm11 + m20 * rm12
        val nm11 = m01 * rm10 + m11 * rm11 + m21 * rm12
        val nm12 = m02 * rm10 + m12 * rm11 + m22 * rm12
        // set non-dependent values directly
        dest.m20 = (m00 * rm20 + m10 * rm21 + m20 * rm22)
        dest.m21 = (m01 * rm20 + m11 * rm21 + m21 * rm22)
        dest.m22 = (m02 * rm20 + m12 * rm21 + m22 * rm22)
        dest.m23 = (0.0f)
        // set other values
        dest.m00 = (nm00)
        dest.m01 = (nm01)
        dest.m02 = (nm02)
        dest.m03 = (0.0f)
        dest.m10 = (nm10)
        dest.m11 = (nm11)
        dest.m12 = (nm12)
        dest.m13 = (0.0f)
        dest.m30 = (m30)
        dest.m31 = (m31)
        dest.m32 = (m32)
        dest.m33 = (1.0f)
        dest.properties = (properties and (PROPERTY_PERSPECTIVE or PROPERTY_IDENTITY or PROPERTY_TRANSLATION).inv())
        return dest
    }

    private fun rotateGeneric(quat: Quaternionfc, dest: Matrix4f): Matrix4f {
        val w2: Float = quat.w * quat.w
        val x2: Float = quat.x * quat.x
        val y2: Float = quat.y * quat.y
        val z2: Float = quat.z * quat.z
        val zw: Float = quat.z * quat.w
        val dzw = zw + zw
        val xy: Float = quat.x * quat.y
        val dxy = xy + xy
        val xz: Float = quat.x * quat.z
        val dxz = xz + xz
        val yw: Float = quat.y * quat.w
        val dyw = yw + yw
        val yz: Float = quat.y * quat.z
        val dyz = yz + yz
        val xw: Float = quat.x * quat.w
        val dxw = xw + xw
        val rm00 = w2 + x2 - z2 - y2
        val rm01 = dxy + dzw
        val rm02 = dxz - dyw
        val rm10 = -dzw + dxy
        val rm11 = y2 - z2 + w2 - x2
        val rm12 = dyz + dxw
        val rm20 = dyw + dxz
        val rm21 = dyz - dxw
        val rm22 = z2 - y2 - x2 + w2
        val nm00 = m00 * rm00 + m10 * rm01 + m20 * rm02
        val nm01 = m01 * rm00 + m11 * rm01 + m21 * rm02
        val nm02 = m02 * rm00 + m12 * rm01 + m22 * rm02
        val nm03 = m03 * rm00 + m13 * rm01 + m23 * rm02
        val nm10 = m00 * rm10 + m10 * rm11 + m20 * rm12
        val nm11 = m01 * rm10 + m11 * rm11 + m21 * rm12
        val nm12 = m02 * rm10 + m12 * rm11 + m22 * rm12
        val nm13 = m03 * rm10 + m13 * rm11 + m23 * rm12
        dest.m20 = (m00 * rm20 + m10 * rm21 + m20 * rm22)
        dest.m21 = (m01 * rm20 + m11 * rm21 + m21 * rm22)
        dest.m22 = (m02 * rm20 + m12 * rm21 + m22 * rm22)
        dest.m23 = (m03 * rm20 + m13 * rm21 + m23 * rm22)
        dest.m00 = (nm00)
        dest.m01 = (nm01)
        dest.m02 = (nm02)
        dest.m03 = (nm03)
        dest.m10 = (nm10)
        dest.m11 = (nm11)
        dest.m12 = (nm12)
        dest.m13 = (nm13)
        dest.m30 = (m30)
        dest.m31 = (m31)
        dest.m32 = (m32)
        dest.m33 = (m33)
        dest.properties = (properties and (PROPERTY_PERSPECTIVE or PROPERTY_IDENTITY or PROPERTY_TRANSLATION).inv())
        return dest
    }

    fun rotateTranslation(quat: Quaternionfc, dest: Matrix4f): Matrix4f {
        val w2: Float = quat.w * quat.w
        val x2: Float = quat.x * quat.x
        val y2: Float = quat.y * quat.y
        val z2: Float = quat.z * quat.z
        val zw: Float = quat.z * quat.w
        val dzw = zw + zw
        val xy: Float = quat.x * quat.y
        val dxy = xy + xy
        val xz: Float = quat.x * quat.z
        val dxz = xz + xz
        val yw: Float = quat.y * quat.w
        val dyw = yw + yw
        val yz: Float = quat.y * quat.z
        val dyz = yz + yz
        val xw: Float = quat.x * quat.w
        val dxw = xw + xw
        val rm00 = w2 + x2 - z2 - y2
        val rm01 = dxy + dzw
        val rm02 = dxz - dyw
        val rm10 = -dzw + dxy
        val rm11 = y2 - z2 + w2 - x2
        val rm12 = dyz + dxw
        val rm20 = dyw + dxz
        val rm21 = dyz - dxw
        val rm22 = z2 - y2 - x2 + w2
        dest.m20 = (rm20)
        dest.m21 = (rm21)
        dest.m22 = (rm22)
        dest.m23 = (0.0f)
        dest.m00 = (rm00)
        dest.m01 = (rm01)
        dest.m02 = (rm02)
        dest.m03 = (0.0f)
        dest.m10 = (rm10)
        dest.m11 = (rm11)
        dest.m12 = (rm12)
        dest.m13 = (0.0f)
        dest.m30 = (m30)
        dest.m31 = (m31)
        dest.m32 = (m32)
        dest.m33 = (m33)
        dest.properties = (properties and (PROPERTY_PERSPECTIVE or PROPERTY_IDENTITY or PROPERTY_TRANSLATION).inv())
        return dest
    }

    fun rotation(quat: Quaternionfc): Matrix4f {
        val w2: Float = quat.w * quat.w
        val x2: Float = quat.x * quat.x
        val y2: Float = quat.y * quat.y
        val z2: Float = quat.z * quat.z
        val zw: Float = quat.z * quat.w
        val dzw = zw + zw
        val xy: Float = quat.x * quat.y
        val dxy = xy + xy
        val xz: Float = quat.x * quat.z
        val dxz = xz + xz
        val yw: Float = quat.y * quat.w
        val dyw = yw + yw
        val yz: Float = quat.y * quat.z
        val dyz = yz + yz
        val xw: Float = quat.x * quat.w
        val dxw = xw + xw
        if (properties and PROPERTY_IDENTITY == 0) identity()
        m00 = (w2 + x2 - z2 - y2)
        m01 = (dxy + dzw)
        m02 = (dxz - dyw)
        m10 = (-dzw + dxy)
        m11 = (y2 - z2 + w2 - x2)
        m12 = (dyz + dxw)
        m20 = (dyw + dxz)
        m21 = (dyz - dxw)
        m22 = (z2 - y2 - x2 + w2)
        properties = (PROPERTY_AFFINE or PROPERTY_ORTHONORMAL)
        return this
    }

    fun rotate(other: Quaternionfc): Matrix4f =
            rotate(other, this)

    fun rotateAffine(other: Quaternionfc): Matrix4f =
            rotateAffine(other, this)

    fun scale(vector: Vector3fc): Matrix4f = scale(vector.x, vector.y, vector.z, this)

    fun perspective(fovy: Float, aspect: Float, zNear: Float, zFar: Float): Matrix4f =
            perspective(fovy, aspect, zNear, zFar, this)

    fun scaling(x: Float, y: Float, z: Float): Matrix4f {
        if (properties and PROPERTY_IDENTITY == 0)
            identity()
        this.m00 = (x)
        this.m11 = (y)
        this.m22 = (z)
        val one = kotlin.math.abs(x) == 1.0f && kotlin.math.abs(y) == 1.0f && kotlin.math.abs(z) == 1.0f
        properties = (PROPERTY_AFFINE or if (one) PROPERTY_ORTHONORMAL else 0)
        return this
    }

    override fun getTranslation(dest: Vector3f): Vector3f {
        dest.x = m30
        dest.y = m31;
        dest.z = m32;
        return dest
    }

    override fun perspective(fovy: Float, aspect: Float, zNear: Float, zFar: Float, zZeroToOne: Boolean, dest: Matrix4f): Matrix4f {
        if (properties and PROPERTY_IDENTITY != 0)
            return dest.setPerspective(fovy, aspect, zNear, zFar, zZeroToOne)
        return perspectiveGeneric(fovy, aspect, zNear, zFar, zZeroToOne, dest);
    }

    private fun perspectiveGeneric(fovy: Float, aspect: Float, zNear: Float, zFar: Float, zZeroToOne: Boolean, dest: Matrix4f): Matrix4f {
        val h = kotlin.math.tan(fovy * 0.5f.toDouble()).toFloat()
        // calculate right matrix elements
        val rm00 = 1.0f / (h * aspect)
        val rm11 = 1.0f / h
        val rm22: Float
        val rm32: Float
        val farInf = zFar > 0 && zFar.isInfinite()
        val nearInf = zNear > 0 && zNear.isInfinite()
        if (farInf) { // See: "Infinite Projection Matrix" (http://www.terathon.com/gdc07_lengyel.pdf)
            val e = 1E-6f
            rm22 = e - 1.0f
            rm32 = (e - if (zZeroToOne) 1.0f else 2.0f) * zNear
        } else if (nearInf) {
            val e = 1E-6f
            rm22 = (if (zZeroToOne) 0.0f else 1.0f) - e
            rm32 = ((if (zZeroToOne) 1.0f else 2.0f) - e) * zFar
        } else {
            rm22 = (if (zZeroToOne) zFar else zFar + zNear) / (zNear - zFar)
            rm32 = (if (zZeroToOne) zFar else zFar + zFar) * zNear / (zNear - zFar)
        }
        // perform optimized matrix multiplication
        val nm20 = m20 * rm22 - m30
        val nm21 = m21 * rm22 - m31
        val nm22 = m22 * rm22 - m32
        val nm23 = m23 * rm22 - m33
        dest.m00 = (m00 * rm00)
        dest.m01 = (m01 * rm00)
        dest.m02 = (m02 * rm00)
        dest.m03 = (m03 * rm00)
        dest.m10 = (m10 * rm11)
        dest.m11 = (m11 * rm11)
        dest.m12 = (m12 * rm11)
        dest.m13 = (m13 * rm11)
        dest.m30 = (m20 * rm32)
        dest.m31 = (m21 * rm32)
        dest.m32 = (m22 * rm32)
        dest.m33 = (m23 * rm32)
        dest.m20 = (nm20)
        dest.m21 = (nm21)
        dest.m22 = (nm22)
        dest.m23 = (nm23)
        dest.properties = (properties and (PROPERTY_AFFINE or PROPERTY_IDENTITY or PROPERTY_TRANSLATION or PROPERTY_ORTHONORMAL).inv())
        return dest
    }

    private fun zero() {
        m00 = 0f
        m01 = 0f
        m02 = 0f
        m03 = 0f
        m10 = 0f
        m11 = 0f
        m12 = 0f
        m13 = 0f
        m20 = 0f
        m21 = 0f
        m22 = 0f
        m23 = 0f
        m30 = 0f
        m31 = 0f
        m32 = 0f
        m33 = 0f
    }

    fun setPerspective(fovy: Float, aspect: Float, zNear: Float, zFar: Float, zZeroToOne: Boolean): Matrix4f {
        zero()
        val h = kotlin.math.tan(fovy * 0.5f.toDouble()).toFloat()
        this.m00 = (1.0f / (h * aspect))
        this.m11 = (1.0f / h)
        val farInf = zFar > 0 && zFar.isInfinite()
        val nearInf = zNear > 0 && zNear.isInfinite()
        if (farInf) { // See: "Infinite Projection Matrix" (http://www.terathon.com/gdc07_lengyel.pdf)
            val e = 1E-6f
            this.m22 = (e - 1.0f)
            this.m32 = ((e - if (zZeroToOne) 1.0f else 2.0f) * zNear)
        } else if (nearInf) {
            val e = 1E-6f
            this.m22 = ((if (zZeroToOne) 0.0f else 1.0f) - e)
            this.m32 = (((if (zZeroToOne) 1.0f else 2.0f) - e) * zFar)
        } else {
            this.m22 = ((if (zZeroToOne) zFar else zFar + zNear) / (zNear - zFar))
            this.m32 = ((if (zZeroToOne) zFar else zFar + zFar) * zNear / (zNear - zFar))
        }
        this.m23 = (-1.0f)
        properties = (PROPERTY_PERSPECTIVE)
        return this
    }

}

const val PI2 = PI * 2
const val PIHalf = PI * 0.5
fun cosFromSin(sin: Double, angle: Double): Double {
//    if (Options.FASTMATH)
    return kotlin.math.sin(angle + PIHalf)
}

fun cosFromSin(sin: Float, angle: Float): Double {
//    if (Options.FASTMATH)
    return kotlin.math.sin(angle + PIHalf)
}