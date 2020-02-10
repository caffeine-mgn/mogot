@file:JvmName("MathCommonKt")

package mogot.math


import kotlin.jvm.JvmName
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin
import kotlin.math.sqrt

interface Matrix4fc {

    companion object {
        /**
         * Argument to the first parameter of [frustumCorner]
         * identifying the corner `(-1, -1, -1)` when using the identity matrix.
         *
         * screen bottom left
         */
        const val CORNER_NXNYNZ = 0

        /**
         * Argument to the first parameter of [frustumCorner]
         * identifying the corner `(1, -1, -1)` when using the identity matrix.
         *
         * screen bottom right
         */
        const val CORNER_PXNYNZ = 1

        /**
         * Argument to the first parameter of [frustumCorner]
         * identifying the corner `(1, 1, -1)` when using the identity matrix.
         *
         * screen top right
         */
        const val CORNER_PXPYNZ = 2

        /**
         * Argument to the first parameter of [frustumCorner]
         * identifying the corner `(-1, 1, -1)` when using the identity matrix.
         *
         * screen top left
         */
        const val CORNER_NXPYNZ = 3

        /**
         * Argument to the first parameter of [frustumCorner]
         * identifying the corner `(1, -1, 1)` when using the identity matrix.
         *
         * projection bottom right
         */
        const val CORNER_PXNYPZ = 4

        /**
         * Argument to the first parameter of [frustumCorner]
         * identifying the corner `(-1, -1, 1)` when using the identity matrix.
         *
         * projection bottom left
         */
        const val CORNER_NXNYPZ = 5

        /**
         * Argument to the first parameter of [frustumCorner]
         * identifying the corner `(-1, 1, 1)` when using the identity matrix.
         *
         * projection top left
         */
        const val CORNER_NXPYPZ = 6
        /**
         * Argument to the first parameter of [frustumCorner]
         * identifying the corner `(1, 1, 1)` when using the identity matrix.
         *
         * projection top right
         */
        const val CORNER_PXPYPZ = 7
    }

    fun getTranslation(dest: Vector3fm = Vector3f()): Vector3fm {
        dest.x = m30
        dest.y = m31
        dest.z = m32
        return dest
    }

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

    fun rotateZ(ang: Float, dest: Matrix4f): Matrix4f {
        if (properties and PROPERTY_IDENTITY != 0) {
            val sin: Float = kotlin.math.sin(ang)
            val cos: Float = cosFromSin(sin, ang)
            if (properties and PROPERTY_IDENTITY == 0) dest.identity()
            dest.m00 = (cos)
            dest.m01 = (sin)
            dest.m10 = (-sin)
            dest.m11 = (cos)
            dest.properties = (PROPERTY_AFFINE or PROPERTY_ORTHONORMAL)
            return dest
        }
        val sin = sin(ang)
        val cos = cosFromSin(sin, ang)
        return rotateTowardsXY(sin, cos, dest)
    }

    fun rotateTowardsXY(dirX: Float, dirY: Float, dest: Matrix4f): Matrix4f {
        if (properties and PROPERTY_IDENTITY != 0) {
            if (properties and PROPERTY_IDENTITY == 0) dest.identity()
            dest.m00 = (dirY)
            dest.m01 = (dirX)
            dest.m10 = (-dirX)
            dest.m11 = (dirY)
            dest.properties = (PROPERTY_AFFINE or PROPERTY_ORTHONORMAL)
            return dest
        }
        val rm10 = -dirX
        val nm00 = m00 * dirY + m10 * dirX
        val nm01 = m01 * dirY + m11 * dirX
        val nm02 = m02 * dirY + m12 * dirX
        val nm03 = m03 * dirY + m13 * dirX
        dest.m10 = (m00 * rm10 + m10 * dirY)
        dest.m11 = (m01 * rm10 + m11 * dirY)
        dest.m12 = (m02 * rm10 + m12 * dirY)
        dest.m13 = (m03 * rm10 + m13 * dirY)
        dest.m00 = (nm00)
        dest.m01 = (nm01)
        dest.m02 = (nm02)
        dest.m03 = (nm03)
        dest.m20 = (m20)
        dest.m21 = (m21)
        dest.m22 = (m22)
        dest.m23 = (m23)
        dest.m30 = (m30)
        dest.m31 = (m31)
        dest.m32 = (m32)
        dest.m33 = (m33)
        dest.properties = (properties and (PROPERTY_PERSPECTIVE or PROPERTY_IDENTITY or PROPERTY_TRANSLATION).inv())
        return dest
    }

    fun ortho2D(left: Float, right: Float, bottom: Float, top: Float, dest: Matrix4f): Matrix4f {
        return if (properties and PROPERTY_IDENTITY != 0)
            dest.setOrtho2D(left, right, bottom, top)
        else
            ortho2DGeneric(left, right, bottom, top, dest)
    }

    fun ortho3D(left: Float, right: Float, bottom: Float, top: Float, zNear: Float, zFar: Float, dest: Matrix4f): Matrix4f{
        return if (properties and PROPERTY_IDENTITY != 0)
            dest.setOrtho3D(left, right, bottom, top,zNear,zFar)
        else
            ortho3DGeneric(left, right, bottom, top,zNear,zFar, dest)
    }

    fun mul(right: Matrix4fc, dest: Matrix4f): Matrix4f {
        if (properties and PROPERTY_IDENTITY != 0)
            return dest.set(right)
        else
            if (right.properties and PROPERTY_IDENTITY != 0)
                return dest.set(this)
            else
                if (properties and PROPERTY_TRANSLATION != 0 && right.properties and PROPERTY_AFFINE != 0)
                    return mulTranslationAffine(right, dest)
                else
                    if (properties and PROPERTY_AFFINE != 0 && right.properties and PROPERTY_AFFINE != 0)
                        return mulAffine(right, dest)
                    else
                        if (properties and PROPERTY_PERSPECTIVE != 0 && right.properties and PROPERTY_AFFINE != 0)
                            return mulPerspectiveAffine(right, dest)
                        else
                            if (right.properties and PROPERTY_AFFINE != 0)
                                return mulAffineR(right, dest)
        return mulGeneric(right, dest)
    }

    fun invert(dest: Matrix4f): Matrix4f {
        if (properties and PROPERTY_IDENTITY != 0) {
            return dest.identity()
        } else if (properties and PROPERTY_TRANSLATION != 0)
            return invertTranslation(dest)
        else
            if (properties and PROPERTY_ORTHONORMAL != 0)
                return invertOrthonormal(dest)
            else
                if (properties and PROPERTY_AFFINE != 0)
                    return invertAffine(dest)
                else
                    if (properties and PROPERTY_PERSPECTIVE != 0)
                        return invertPerspective(dest)
        return invertGeneric(dest)
    }

    fun getScale(dest: Vector3fm): Vector3fm {
        dest.x = sqrt(m00 * m00 + m01 * m01 + (m02 * m02))
        dest.y = sqrt(m10 * m10 + m11 * m11 + (m12 * m12))
        dest.z = sqrt(m20 * m20 + m21 * m21 + (m22 * m22))
        return dest
    }
}

private fun Matrix4fc.invertGeneric(dest: Matrix4f): Matrix4f {
    val a = m00 * m11 - m01 * m10
    val b = m00 * m12 - m02 * m10
    val c = m00 * m13 - m03 * m10
    val d = m01 * m12 - m02 * m11
    val e = m01 * m13 - m03 * m11
    val f = m02 * m13 - m03 * m12
    val g = m20 * m31 - m21 * m30
    val h = m20 * m32 - m22 * m30
    val i = m20 * m33 - m23 * m30
    val j = m21 * m32 - m22 * m31
    val k = m21 * m33 - m23 * m31
    val l = m22 * m33 - m23 * m32
    var det = a * l - b * k + c * j + d * i - e * h + f * g
    det = 1.0f / det
    val nm00: Float = Math.fma(m11, l, Math.fma(-m12, k, m13 * j)) * det
    val nm01: Float = Math.fma(-m01, l, Math.fma(m02, k, -m03 * j)) * det
    val nm02: Float = Math.fma(m31, f, Math.fma(-m32, e, m33 * d)) * det
    val nm03: Float = Math.fma(-m21, f, Math.fma(m22, e, -m23 * d)) * det
    val nm10: Float = Math.fma(-m10, l, Math.fma(m12, i, -m13 * h)) * det
    val nm11: Float = Math.fma(m00, l, Math.fma(-m02, i, m03 * h)) * det
    val nm12: Float = Math.fma(-m30, f, Math.fma(m32, c, -m33 * b)) * det
    val nm13: Float = Math.fma(m20, f, Math.fma(-m22, c, m23 * b)) * det
    val nm20: Float = Math.fma(m10, k, Math.fma(-m11, i, m13 * g)) * det
    val nm21: Float = Math.fma(-m00, k, Math.fma(m01, i, -m03 * g)) * det
    val nm22: Float = Math.fma(m30, e, Math.fma(-m31, c, m33 * a)) * det
    val nm23: Float = Math.fma(-m20, e, Math.fma(m21, c, -m23 * a)) * det
    val nm30: Float = Math.fma(-m10, j, Math.fma(m11, h, -m12 * g)) * det
    val nm31: Float = Math.fma(m00, j, Math.fma(-m01, h, m02 * g)) * det
    val nm32: Float = Math.fma(-m30, d, Math.fma(m31, b, -m32 * a)) * det
    val nm33: Float = Math.fma(m20, d, Math.fma(-m21, b, m22 * a)) * det
    dest.m00 = nm00
    dest.m01 = nm01
    dest.m02 = nm02
    dest.m03 = nm03
    dest.m10 = nm10
    dest.m11 = nm11
    dest.m12 = nm12
    dest.m13 = nm13
    dest.m20 = nm20
    dest.m21 = nm21
    dest.m22 = nm22
    dest.m23 = nm23
    dest.m30 = nm30
    dest.m31 = nm31
    dest.m32 = nm32
    dest.m33 = nm33
    dest.properties = 0
    return dest
}

private fun Matrix4fc.invertPerspective(dest: Matrix4f): Matrix4f {
    val a: Float = 1.0f / (m00 * m11)
    val l: Float = -1.0f / (m23 * m32)
    dest.m00 = m11 * a
    dest.m01 = 0f
    dest.m02 = 0f
    dest.m03 = 0f
    dest.m10 = 0f
    dest.m11 = m00 * a
    dest.m12 = 0f
    dest.m13 = 0f
    dest.m20 = 0f
    dest.m21 = 0f
    dest.m22 = 0f
    dest.m23 = -m23 * l
    dest.m30 = 0f
    dest.m31 = 0f
    dest.m32 = -m32 * l
    dest.m33 = m22 * l
    dest.properties = (0)
    return dest
}

private fun Matrix4fc.invertPerspectiveView(view: Matrix4fc, dest: Matrix4f): Matrix4f {
    val a: Float = 1.0f / (m00 * m11)
    val l: Float = -1.0f / (m23 * m32)
    val pm00: Float = m11 * a
    val pm11: Float = m00 * a
    val pm23: Float = -m23 * l
    val pm32: Float = -m32 * l
    val pm33: Float = m22 * l
    val vm30: Float = -view.m00 * view.m30 - view.m01 * view.m31 - view.m02 * view.m32
    val vm31: Float = -view.m10 * view.m30 - view.m11 * view.m31 - view.m12 * view.m32
    val vm32: Float = -view.m20 * view.m30 - view.m21 * view.m31 - view.m22 * view.m32
    val nm00: Float = view.m00 * pm00
    val nm01: Float = view.m10 * pm00
    val nm02: Float = view.m20 * pm00
    val nm10: Float = view.m01 * pm11
    val nm11: Float = view.m11 * pm11
    val nm12: Float = view.m21 * pm11
    val nm20 = vm30 * pm23
    val nm21 = vm31 * pm23
    val nm22 = vm32 * pm23
    val nm30: Float = view.m02 * pm32 + vm30 * pm33
    val nm31: Float = view.m12 * pm32 + vm31 * pm33
    val nm32: Float = view.m22 * pm32 + vm32 * pm33
    dest.m00 = nm00
    dest.m01 = nm01
    dest.m02 = nm02
    dest.m03 = 0.0f
    dest.m10 = nm10
    dest.m11 = nm11
    dest.m12 = nm12
    dest.m13 = 0.0f
    dest.m20 = nm20
    dest.m21 = nm21
    dest.m22 = nm22
    dest.m23 = pm23
    dest.m30 = nm30
    dest.m31 = nm31
    dest.m32 = nm32
    dest.m33 = pm33
    dest.properties = 0
    return dest
}

private fun Matrix4fc.invertAffine(dest: Matrix4f): Matrix4f {
    val m11m00: Float = m00 * m11
    val m10m01: Float = m01 * m10
    val m10m02: Float = m02 * m10
    val m12m00: Float = m00 * m12
    val m12m01: Float = m01 * m12
    val m11m02: Float = m02 * m11
    val det: Float = (m11m00 - m10m01) * m22 + (m10m02 - m12m00) * m21 + (m12m01 - m11m02) * m20
    val s = 1.0f / det
    val nm00: Float
    val nm01: Float
    val nm02: Float
    val nm10: Float
    val nm11: Float
    val nm12: Float
    val nm20: Float
    val nm21: Float
    val nm22: Float
    val nm30: Float
    val nm31: Float
    val nm32: Float
    val m10m22: Float = m10 * m22
    val m10m21: Float = m10 * m21
    val m11m22: Float = m11 * m22
    val m11m20: Float = m11 * m20
    val m12m21: Float = m12 * m21
    val m12m20: Float = m12 * m20
    val m20m02: Float = m20 * m02
    val m20m01: Float = m20 * m01
    val m21m02: Float = m21 * m02
    val m21m00: Float = m21 * m00
    val m22m01: Float = m22 * m01
    val m22m00: Float = m22 * m00
    nm00 = (m11m22 - m12m21) * s
    nm01 = (m21m02 - m22m01) * s
    nm02 = (m12m01 - m11m02) * s
    nm10 = (m12m20 - m10m22) * s
    nm11 = (m22m00 - m20m02) * s
    nm12 = (m10m02 - m12m00) * s
    nm20 = (m10m21 - m11m20) * s
    nm21 = (m20m01 - m21m00) * s
    nm22 = (m11m00 - m10m01) * s
    nm30 = (m10m22 * m31 - m10m21 * m32 + m11m20 * m32 - m11m22 * m30 + m12m21 * m30 - m12m20 * m31) * s
    nm31 = (m20m02 * m31 - m20m01 * m32 + m21m00 * m32 - m21m02 * m30 + m22m01 * m30 - m22m00 * m31) * s
    nm32 = (m11m02 * m30 - m12m01 * m30 + m12m00 * m31 - m10m02 * m31 + m10m01 * m32 - m11m00 * m32) * s
    dest.m00 = (nm00)
    dest.m01 = (nm01)
    dest.m02 = (nm02)
    dest.m03 = (0.0f)
    dest.m10 = (nm10)
    dest.m11 = (nm11)
    dest.m12 = (nm12)
    dest.m13 = (0.0f)
    dest.m20 = (nm20)
    dest.m21 = (nm21)
    dest.m22 = (nm22)
    dest.m23 = (0.0f)
    dest.m30 = (nm30)
    dest.m31 = (nm31)
    dest.m32 = (nm32)
    dest.m33 = (1.0f)
    dest.properties = (PROPERTY_AFFINE.toInt())
    return dest
}

private fun Matrix4fc.invertOrthonormal(dest: Matrix4f): Matrix4f {
    val nm30: Float = -(m00 * m30 + m01 * m31 + m02 * m32)
    val nm31: Float = -(m10 * m30 + m11 * m31 + m12 * m32)
    val nm32: Float = -(m20 * m30 + m21 * m31 + m22 * m32)
    val m01: Float = this.m01
    val m02: Float = this.m02
    val m12: Float = this.m12
    dest.m00 = (m00)
    dest.m01 = (m10)
    dest.m02 = (m20)
    dest.m03 = (0.0f)
    dest.m10 = (m01)
    dest.m11 = (m11)
    dest.m12 = (m21)
    dest.m13 = (0.0f)
    dest.m20 = (m02)
    dest.m21 = (m12)
    dest.m22 = (m22)
    dest.m23 = (0.0f)
    dest.m30 = (nm30)
    dest.m31 = (nm31)
    dest.m32 = (nm32)
    dest.m33 = (1.0f)
    dest.properties = (PROPERTY_AFFINE or PROPERTY_ORTHONORMAL)
    return dest
}

private fun Matrix4fc.invertTranslation(dest: Matrix4f): Matrix4f {
    if (dest !== this)
        dest.set(this)
    dest.m30 = -m30
    dest.m31 = -m31
    dest.m32 = -m32
    dest.properties = (PROPERTY_AFFINE or PROPERTY_TRANSLATION or PROPERTY_ORTHONORMAL)
    return dest
}

private fun Matrix4fc.mulGeneric(right: Matrix4fc, dest: Matrix4f): Matrix4f {
    val nm00: Float = Math.fma(m00, right.m00, Math.fma(m10, right.m01, Math.fma(m20, right.m02, m30 * right.m03)))
    val nm01: Float = Math.fma(m01, right.m00, Math.fma(m11, right.m01, Math.fma(m21, right.m02, m31 * right.m03)))
    val nm02: Float = Math.fma(m02, right.m00, Math.fma(m12, right.m01, Math.fma(m22, right.m02, m32 * right.m03)))
    val nm03: Float = Math.fma(m03, right.m00, Math.fma(m13, right.m01, Math.fma(m23, right.m02, m33 * right.m03)))
    val nm10: Float = Math.fma(m00, right.m10, Math.fma(m10, right.m11, Math.fma(m20, right.m12, m30 * right.m13)))
    val nm11: Float = Math.fma(m01, right.m10, Math.fma(m11, right.m11, Math.fma(m21, right.m12, m31 * right.m13)))
    val nm12: Float = Math.fma(m02, right.m10, Math.fma(m12, right.m11, Math.fma(m22, right.m12, m32 * right.m13)))
    val nm13: Float = Math.fma(m03, right.m10, Math.fma(m13, right.m11, Math.fma(m23, right.m12, m33 * right.m13)))
    val nm20: Float = Math.fma(m00, right.m20, Math.fma(m10, right.m21, Math.fma(m20, right.m22, m30 * right.m23)))
    val nm21: Float = Math.fma(m01, right.m20, Math.fma(m11, right.m21, Math.fma(m21, right.m22, m31 * right.m23)))
    val nm22: Float = Math.fma(m02, right.m20, Math.fma(m12, right.m21, Math.fma(m22, right.m22, m32 * right.m23)))
    val nm23: Float = Math.fma(m03, right.m20, Math.fma(m13, right.m21, Math.fma(m23, right.m22, m33 * right.m23)))
    val nm30: Float = Math.fma(m00, right.m30, Math.fma(m10, right.m31, Math.fma(m20, right.m32, m30 * right.m33)))
    val nm31: Float = Math.fma(m01, right.m30, Math.fma(m11, right.m31, Math.fma(m21, right.m32, m31 * right.m33)))
    val nm32: Float = Math.fma(m02, right.m30, Math.fma(m12, right.m31, Math.fma(m22, right.m32, m32 * right.m33)))
    val nm33: Float = Math.fma(m03, right.m30, Math.fma(m13, right.m31, Math.fma(m23, right.m32, m33 * right.m33)))
    dest.m00 = (nm00)
    dest.m01 = (nm01)
    dest.m02 = (nm02)
    dest.m03 = (nm03)
    dest.m10 = (nm10)
    dest.m11 = (nm11)
    dest.m12 = (nm12)
    dest.m13 = (nm13)
    dest.m20 = (nm20)
    dest.m21 = (nm21)
    dest.m22 = (nm22)
    dest.m23 = (nm23)
    dest.m30 = (nm30)
    dest.m31 = (nm31)
    dest.m32 = (nm32)
    dest.m33 = (nm33)
    dest.properties = (0)
    return dest
}

fun Matrix4fc.mulAffineR(right: Matrix4fc, dest: Matrix4f): Matrix4f {
    val nm00: Float = m00 * right.m00 + m10 * right.m01 + m20 * right.m02
    val nm01: Float = m01 * right.m00 + m11 * right.m01 + m21 * right.m02
    val nm02: Float = m02 * right.m00 + m12 * right.m01 + m22 * right.m02
    val nm03: Float = m03 * right.m00 + m13 * right.m01 + m23 * right.m02
    val nm10: Float = m00 * right.m10 + m10 * right.m11 + m20 * right.m12
    val nm11: Float = m01 * right.m10 + m11 * right.m11 + m21 * right.m12
    val nm12: Float = m02 * right.m10 + m12 * right.m11 + m22 * right.m12
    val nm13: Float = m03 * right.m10 + m13 * right.m11 + m23 * right.m12
    val nm20: Float = m00 * right.m20 + m10 * right.m21 + m20 * right.m22
    val nm21: Float = m01 * right.m20 + m11 * right.m21 + m21 * right.m22
    val nm22: Float = m02 * right.m20 + m12 * right.m21 + m22 * right.m22
    val nm23: Float = m03 * right.m20 + m13 * right.m21 + m23 * right.m22
    val nm30: Float = m00 * right.m30 + m10 * right.m31 + m20 * right.m32 + m30
    val nm31: Float = m01 * right.m30 + m11 * right.m31 + m21 * right.m32 + m31
    val nm32: Float = m02 * right.m30 + m12 * right.m31 + m22 * right.m32 + m32
    val nm33: Float = m03 * right.m30 + m13 * right.m31 + m23 * right.m32 + m33
    dest.m00 = (nm00)
    dest.m01 = (nm01)
    dest.m02 = (nm02)
    dest.m03 = (nm03)
    dest.m10 = (nm10)
    dest.m11 = (nm11)
    dest.m12 = (nm12)
    dest.m13 = (nm13)
    dest.m20 = (nm20)
    dest.m21 = (nm21)
    dest.m22 = (nm22)
    dest.m23 = (nm23)
    dest.m30 = (nm30)
    dest.m31 = (nm31)
    dest.m32 = (nm32)
    dest.m33 = (nm33)
    dest.properties = (properties and (PROPERTY_IDENTITY or PROPERTY_PERSPECTIVE or PROPERTY_TRANSLATION or PROPERTY_ORTHONORMAL).inv())
    return dest
}

fun Matrix4fc.mulPerspectiveAffine(view: Matrix4fc, dest: Matrix4f): Matrix4f {
    val nm00: Float = m00 * view.m00
    val nm01: Float = m11 * view.m01
    val nm02: Float = m22 * view.m02
    val nm03: Float = m23 * view.m02
    val nm10: Float = m00 * view.m10
    val nm11: Float = m11 * view.m11
    val nm12: Float = m22 * view.m12
    val nm13: Float = m23 * view.m12
    val nm20: Float = m00 * view.m20
    val nm21: Float = m11 * view.m21
    val nm22: Float = m22 * view.m22
    val nm23: Float = m23 * view.m22
    val nm30: Float = m00 * view.m30
    val nm31: Float = m11 * view.m31
    val nm32: Float = m22 * view.m32 + m32
    val nm33: Float = m23 * view.m32
    dest.m00 = (nm00)
    dest.m01 = (nm01)
    dest.m02 = (nm02)
    dest.m03 = (nm03)
    dest.m10 = (nm10)
    dest.m11 = (nm11)
    dest.m12 = (nm12)
    dest.m13 = (nm13)
    dest.m20 = (nm20)
    dest.m21 = (nm21)
    dest.m22 = (nm22)
    dest.m23 = (nm23)
    dest.m30 = (nm30)
    dest.m31 = (nm31)
    dest.m32 = (nm32)
    dest.m33 = (nm33)
    dest.properties = (0)
    return dest
}

private fun Matrix4fc.mulAffine(right: Matrix4fc, dest: Matrix4f): Matrix4f {
    val nm00: Float = m00 * right.m00 + m10 * right.m01 + m20 * right.m02
    val nm01: Float = m01 * right.m00 + m11 * right.m01 + m21 * right.m02
    val nm02: Float = m02 * right.m00 + m12 * right.m01 + m22 * right.m02
    val nm03: Float = m03
    val nm10: Float = m00 * right.m10 + m10 * right.m11 + m20 * right.m12
    val nm11: Float = m01 * right.m10 + m11 * right.m11 + m21 * right.m12
    val nm12: Float = m02 * right.m10 + m12 * right.m11 + m22 * right.m12
    val nm13: Float = m13
    val nm20: Float = m00 * right.m20 + m10 * right.m21 + m20 * right.m22
    val nm21: Float = m01 * right.m20 + m11 * right.m21 + m21 * right.m22
    val nm22: Float = m02 * right.m20 + m12 * right.m21 + m22 * right.m22
    val nm23: Float = m23
    val nm30: Float = m00 * right.m30 + m10 * right.m31 + m20 * right.m32 + m30
    val nm31: Float = m01 * right.m30 + m11 * right.m31 + m21 * right.m32 + m31
    val nm32: Float = m02 * right.m30 + m12 * right.m31 + m22 * right.m32 + m32
    val nm33: Float = m33
    dest.m00 = (nm00)
    dest.m01 = (nm01)
    dest.m02 = (nm02)
    dest.m03 = (nm03)
    dest.m10 = (nm10)
    dest.m11 = (nm11)
    dest.m12 = (nm12)
    dest.m13 = (nm13)
    dest.m20 = (nm20)
    dest.m21 = (nm21)
    dest.m22 = (nm22)
    dest.m23 = (nm23)
    dest.m30 = (nm30)
    dest.m31 = (nm31)
    dest.m32 = (nm32)
    dest.m33 = (nm33)
    dest.properties = (PROPERTY_AFFINE or (this.properties and right.properties and PROPERTY_ORTHONORMAL))
    return dest
}

private fun Matrix4fc.mulTranslationAffine(right: Matrix4fc, dest: Matrix4f): Matrix4f {
    val nm00: Float = right.m00
    val nm01: Float = right.m01
    val nm02: Float = right.m02
    val nm03: Float = m03
    val nm10: Float = right.m10
    val nm11: Float = right.m11
    val nm12: Float = right.m12
    val nm13: Float = m13
    val nm20: Float = right.m20
    val nm21: Float = right.m21
    val nm22: Float = right.m22
    val nm23: Float = m23
    val nm30: Float = right.m30 + m30
    val nm31: Float = right.m31 + m31
    val nm32: Float = right.m32 + m32
    val nm33: Float = m33
    dest.m00 = (nm00)
    dest.m01 = (nm01)
    dest.m02 = (nm02)
    dest.m03 = (nm03)
    dest.m10 = (nm10)
    dest.m11 = (nm11)
    dest.m12 = (nm12)
    dest.m13 = (nm13)
    dest.m20 = (nm20)
    dest.m21 = (nm21)
    dest.m22 = (nm22)
    dest.m23 = (nm23)
    dest.m30 = (nm30)
    dest.m31 = (nm31)
    dest.m32 = (nm32)
    dest.m33 = (nm33)
    dest.properties = (PROPERTY_AFFINE or (right.properties and PROPERTY_ORTHONORMAL))
    return dest
}

private fun Matrix4fc.ortho2DGeneric(left: Float, right: Float, bottom: Float, top: Float, dest: Matrix4f): Matrix4f { // calculate right matrix elements
    val rm00 = 2.0f / (right - left)
    val rm11 = 2.0f / (top - bottom)
    val rm30 = (right + left) / (left - right)
    val rm31 = (top + bottom) / (bottom - top)
    // perform optimized multiplication
// compute the last column first, because other columns do not depend on it
    dest.m30 = (m00 * rm30 + m10 * rm31 + m30)
    dest.m31 = (m01 * rm30 + m11 * rm31 + m31)
    dest.m32 = (m02 * rm30 + m12 * rm31 + m32)
    dest.m33 = (m03 * rm30 + m13 * rm31 + m33)
    dest.m00 = (m00 * rm00)
    dest.m01 = (m01 * rm00)
    dest.m02 = (m02 * rm00)
    dest.m03 = (m03 * rm00)
    dest.m10 = (m10 * rm11)
    dest.m11 = (m11 * rm11)
    dest.m12 = (m12 * rm11)
    dest.m13 = (m13 * rm11)
    dest.m20 = (-m20)
    dest.m21 = (-m21)
    dest.m22 = (-m22)
    dest.m23 = (-m23)
    dest.properties = (properties and (PROPERTY_PERSPECTIVE or PROPERTY_IDENTITY or PROPERTY_TRANSLATION or PROPERTY_ORTHONORMAL).inv())
    return dest
}

private fun Matrix4fc.ortho3DGeneric(left: Float, right: Float, bottom: Float, top: Float, zNear: Float, zFar: Float, dest: Matrix4f): Matrix4f{
    val rm00 = 2.0f / (right - left)
    val rm11 = 1.0f / (top - bottom)
    val rm22 = -2.0f / (zFar - zNear)
    val rm30 = (right + left) / (left - right)
    val rm31 = (top + bottom) / (bottom - top)
    val rm32 = (zFar + zNear) / (zNear - zFar)
    // perform optimized multiplication
// compute the last column first, because other columns do not depend on it
    dest.m30 = (m00 * rm30 + m10 * rm31 + m30 * rm32)
    dest.m31 = (m01 * rm30 + m11 * rm31 + m31 * rm32)
    dest.m32 = (m02 * rm30 + m12 * rm31 + m32 * rm32)
    dest.m33 = (m03 * rm30 + m13 * rm31 + m33 * rm32)
    dest.m00 = (m00 * rm00)
    dest.m01 = (m01 * rm00)
    dest.m02 = (m02 * rm00)
    dest.m03 = (m03 * rm00)
    dest.m10 = (m10 * rm11)
    dest.m11 = (m11 * rm11)
    dest.m12 = (m12 * rm11)
    dest.m13 = (m13 * rm11)
    dest.m20 = (m20 * rm22)
    dest.m21 = (m21 * rm22)
    dest.m22 = (m22 * rm22)
    dest.m23 = (m23 * rm22)
    dest.properties = (properties and (PROPERTY_PERSPECTIVE or PROPERTY_IDENTITY or PROPERTY_TRANSLATION or PROPERTY_ORTHONORMAL).inv())
    return dest
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

    constructor()
    constructor(other: Matrix4fc) {
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
    }

    fun invert(): Matrix4f = invert(this)

    fun rotateZ(ang: Float): Matrix4f = rotateZ(ang, this)

    fun ortho2D(left: Float, right: Float, bottom: Float, top: Float): Matrix4f =
            ortho2D(left, right, bottom, top, this)
    fun ortho3D(left: Float, right: Float, bottom: Float, top: Float, zNear: Float, zFar: Float): Matrix4f =
            ortho3D(left, right, bottom, top,zNear,zFar,this)

    internal fun setOrtho2D(left: Float, right: Float, bottom: Float, top: Float): Matrix4f {
        if (properties and PROPERTY_IDENTITY == 0) identity()
        this.m00 = (2.0f / (right - left))
        this.m11 = (2.0f / (top - bottom))
        this.m22 = (-1.0f)
        this.m30 = ((right + left) / (left - right))
        this.m31 = ((top + bottom) / (bottom - top))
        properties = PROPERTY_AFFINE
        return this
    }

    internal fun setOrtho3D(left: Float, right: Float, bottom: Float, top: Float, zNear: Float, zFar: Float): Matrix4f {
        if (properties and PROPERTY_IDENTITY == 0) identity()
        this.m00 = (2.0f / (right - left))
        this.m11 = (1.0f / (top - bottom))
        this.m22 = (-2.0f / (zFar - zNear))
        this.m30 = ((right + left) / (left - right))
        this.m31 = ((top + bottom) / (bottom - top))
        this.m33 = ((zFar+zNear)/(zNear-zFar))
        properties = PROPERTY_AFFINE
        return this
    }

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

    fun translate(x: Float, y: Float, z: Float): Matrix4f {
        if (properties and PROPERTY_IDENTITY != 0) return translation(x, y, z)
        this.m30 = (m00 * x + m10 * y + m20 * z + m30)
        this.m31 = (m01 * x + m11 * y + m21 * z + m31)
        this.m32 = (m02 * x + m12 * y + m22 * z + m32)
        this.m33 = (m03 * x + m13 * y + m23 * z + m33)
        properties = properties and (PROPERTY_PERSPECTIVE or PROPERTY_IDENTITY).inv()
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
        val s = kotlin.math.sin(ang)
        val c = cosFromSin(s, ang)
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
    fun scale(x: Float, y: Float, z: Float): Matrix4f = scale(x, y, z, this)

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

const val PIf = PI.toFloat()
const val PI2 = PI * 2
const val PIHalf = PIf * 0.5f
const val PIHalf2 = PI * 0.5
fun cosFromSin(sin: Double, angle: Double): Double {
//    if (Options.FASTMATH)
    return kotlin.math.sin(angle + PIHalf2)
}

fun cosFromSin(sin: Float, angle: Float): Float {
//    if (Options.FASTMATH)
    return kotlin.math.sin(angle + PIHalf)
}

operator fun Matrix4fc.times(viewMatrix: Matrix4fc): Matrix4f = mul(viewMatrix, Matrix4f())

fun Matrix4f.translationRotateScale(t: Vector3fc,
                                    q: Quaternionfc,
                                    s: Vector3fc) =
        translationRotateScale(
                t.x, t.y, t.z,
                q.x, q.y, q.z, q.w,
                s.x, s.y, s.z
        )


fun Matrix4f.translationRotateScale(tx: Float, ty: Float, tz: Float,
                                    qx: Float, qy: Float, qz: Float, qw: Float,
                                    sx: Float, sy: Float, sz: Float): Matrix4f {
    val dqx = qx + qx
    val dqy = qy + qy
    val dqz = qz + qz
    val q00 = dqx * qx
    val q11 = dqy * qy
    val q22 = dqz * qz
    val q01 = dqx * qy
    val q02 = dqx * qz
    val q03 = dqx * qw
    val q12 = dqy * qz
    val q13 = dqy * qw
    val q23 = dqz * qw
    this.m00 = sx - (q11 + q22) * sx
    this.m01 = (q01 + q23) * sx
    this.m02 = (q02 - q13) * sx
    this.m03 = 0.0f
    this.m10 = (q01 - q23) * sy
    this.m11 = sy - (q22 + q00) * sy
    this.m12 = (q12 + q03) * sy
    this.m13 = 0.0f
    this.m20 = (q02 + q13) * sz
    this.m21 = (q12 - q03) * sz
    this.m22 = sz - (q11 + q00) * sz
    this.m23 = 0.0f
    this.m30 = tx
    this.m31 = ty
    this.m32 = tz
    this.m33 = 1.0f
    val one = abs(sx) == 1.0f && abs(sy) == 1.0f && abs(sz) == 1.0f
    properties = PROPERTY_AFFINE or if (one) PROPERTY_ORTHONORMAL else 0
    return this
}

fun Matrix4fc.unprojectRay(winX: Float, winY: Float, viewport: IntArray, originDest: Vector3f, dirDest: Vector3fm): Matrix4fc {
    val a = m00 * m11 - m01 * m10
    val b = m00 * m12 - m02 * m10
    val c = m00 * m13 - m03 * m10
    val d = m01 * m12 - m02 * m11
    val e = m01 * m13 - m03 * m11
    val f = m02 * m13 - m03 * m12
    val g = m20 * m31 - m21 * m30
    val h = m20 * m32 - m22 * m30
    val i = m20 * m33 - m23 * m30
    val j = m21 * m32 - m22 * m31
    val k = m21 * m33 - m23 * m31
    val l = m22 * m33 - m23 * m32
    var det = a * l - b * k + c * j + d * i - e * h + f * g
    det = 1.0f / det
    val im00 = (m11 * l - m12 * k + m13 * j) * det
    val im01 = (-m01 * l + m02 * k - m03 * j) * det
    val im02 = (m31 * f - m32 * e + m33 * d) * det
    val im03 = (-m21 * f + m22 * e - m23 * d) * det
    val im10 = (-m10 * l + m12 * i - m13 * h) * det
    val im11 = (m00 * l - m02 * i + m03 * h) * det
    val im12 = (-m30 * f + m32 * c - m33 * b) * det
    val im13 = (m20 * f - m22 * c + m23 * b) * det
    val im20 = (m10 * k - m11 * i + m13 * g) * det
    val im21 = (-m00 * k + m01 * i - m03 * g) * det
    val im22 = (m30 * e - m31 * c + m33 * a) * det
    val im23 = (-m20 * e + m21 * c - m23 * a) * det
    val im30 = (-m10 * j + m11 * h - m12 * g) * det
    val im31 = (m00 * j - m01 * h + m02 * g) * det
    val im32 = (-m30 * d + m31 * b - m32 * a) * det
    val im33 = (m20 * d - m21 * b + m22 * a) * det
    val ndcX = (winX - viewport[0]) / viewport[2] * 2.0f - 1.0f
    val ndcY = (winY - viewport[1]) / viewport[3] * 2.0f - 1.0f
    val px = im00 * ndcX + im10 * ndcY + im30
    val py = im01 * ndcX + im11 * ndcY + im31
    val pz = im02 * ndcX + im12 * ndcY + im32
    val invNearW = 1.0f / (im03 * ndcX + im13 * ndcY - im23 + im33)
    val nearX = (px - im20) * invNearW
    val nearY = (py - im21) * invNearW
    val nearZ = (pz - im22) * invNearW
    val invW0 = 1.0f / (im03 * ndcX + im13 * ndcY + im33)
    val x0 = px * invW0
    val y0 = py * invW0
    val z0 = pz * invW0
    originDest.x = nearX
    originDest.y = nearY
    originDest.z = nearZ
    dirDest.x = x0 - nearX
    dirDest.y = y0 - nearY
    dirDest.z = z0 - nearZ
    return this
}

fun Matrix4fc.unproject(winX: Float, winY: Float, winZ: Float, viewport: IntArray, dest: Vector3f): Vector3f {
    val a: Float = m00 * m11 - m01 * m10
    val b: Float = m00 * m12 - m02 * m10
    val c: Float = m00 * m13 - m03 * m10
    val d: Float = m01 * m12 - m02 * m11
    val e: Float = m01 * m13 - m03 * m11
    val f: Float = m02 * m13 - m03 * m12
    val g: Float = m20 * m31 - m21 * m30
    val h: Float = m20 * m32 - m22 * m30
    val i: Float = m20 * m33 - m23 * m30
    val j: Float = m21 * m32 - m22 * m31
    val k: Float = m21 * m33 - m23 * m31
    val l: Float = m22 * m33 - m23 * m32
    var det = a * l - b * k + c * j + d * i - e * h + f * g
    det = 1.0f / det
    val im00: Float = (m11 * l - m12 * k + m13 * j) * det
    val im01: Float = (-m01 * l + m02 * k - m03 * j) * det
    val im02: Float = (m31 * f - m32 * e + m33 * d) * det
    val im03: Float = (-m21 * f + m22 * e - m23 * d) * det
    val im10: Float = (-m10 * l + m12 * i - m13 * h) * det
    val im11: Float = (m00 * l - m02 * i + m03 * h) * det
    val im12: Float = (-m30 * f + m32 * c - m33 * b) * det
    val im13: Float = (m20 * f - m22 * c + m23 * b) * det
    val im20: Float = (m10 * k - m11 * i + m13 * g) * det
    val im21: Float = (-m00 * k + m01 * i - m03 * g) * det
    val im22: Float = (m30 * e - m31 * c + m33 * a) * det
    val im23: Float = (-m20 * e + m21 * c - m23 * a) * det
    val im30: Float = (-m10 * j + m11 * h - m12 * g) * det
    val im31: Float = (m00 * j - m01 * h + m02 * g) * det
    val im32: Float = (-m30 * d + m31 * b - m32 * a) * det
    val im33: Float = (m20 * d - m21 * b + m22 * a) * det
    val ndcX = (winX - viewport[0]) / viewport[2] * 2.0f - 1.0f
    val ndcY = (winY - viewport[1]) / viewport[3] * 2.0f - 1.0f
    val ndcZ = winZ + winZ - 1.0f
    val invW = 1.0f / (im03 * ndcX + im13 * ndcY + im23 * ndcZ + im33)
    dest.x = (im00 * ndcX + im10 * ndcY + im20 * ndcZ + im30) * invW
    dest.y = (im01 * ndcX + im11 * ndcY + im21 * ndcZ + im31) * invW
    dest.z = (im02 * ndcX + im12 * ndcY + im22 * ndcZ + im32) * invW
    return dest
}

fun Matrix4f.mul(right: Matrix4fc): Matrix4f = mul(right, this)

fun Matrix4fc.unprojectInvRay(winX: Float, winY: Float, viewport: IntArray, originDest: Vector3fm, dirDest: Vector3fm): Matrix4fc {
    val ndcX = (winX - viewport[0]) / viewport[2] * 2.0f - 1.0f
    val ndcY = (winY - viewport[1]) / viewport[3] * 2.0f - 1.0f
    val px: Float = m00 * ndcX + m10 * ndcY + m30
    val py: Float = m01 * ndcX + m11 * ndcY + m31
    val pz: Float = m02 * ndcX + m12 * ndcY + m32
    val invNearW: Float = 1.0f / (m03 * ndcX + m13 * ndcY - m23 + m33)
    val nearX: Float = (px - m20) * invNearW
    val nearY: Float = (py - m21) * invNearW
    val nearZ: Float = (pz - m22) * invNearW
    val invFarW: Float = 1.0f / (m03 * ndcX + m13 * ndcY + m23 + m33)
    val farX: Float = (px + m20) * invFarW
    val farY: Float = (py + m21) * invFarW
    val farZ: Float = (pz + m22) * invFarW
    originDest.x = nearX
    originDest.y = nearY
    originDest.z = nearZ
    dirDest.x = farX - nearX
    dirDest.y = farY - nearY
    dirDest.z = farZ - nearZ
    return this
}

fun Matrix4fc.unprojectInv(winX: Float, winY: Float, winZ: Float, viewport: IntArray, dest: Vector4f): Vector4f {
    val ndcX = (winX - viewport[0]) / viewport[2] * 2.0f - 1.0f
    val ndcY = (winY - viewport[1]) / viewport[3] * 2.0f - 1.0f
    val ndcZ = winZ + winZ - 1.0f
    val invW: Float = 1.0f / (m03 * ndcX + m13 * ndcY + m23 * ndcZ + m33)
    dest.x = (m00 * ndcX + m10 * ndcY + m20 * ndcZ + m30) * invW
    dest.y = (m01 * ndcX + m11 * ndcY + m21 * ndcZ + m31) * invW
    dest.z = (m02 * ndcX + m12 * ndcY + m22 * ndcZ + m32) * invW
    dest.w = 1.0f
    return dest
}

fun Matrix4fc.unprojectInv(winX: Float, winY: Float, winZ: Float, viewport: IntArray, dest: Vector3fm): Vector3fm {
    val ndcX = (winX - viewport[0]) / viewport[2] * 2.0f - 1.0f
    val ndcY = (winY - viewport[1]) / viewport[3] * 2.0f - 1.0f
    val ndcZ = winZ + winZ - 1.0f
    val invW: Float = 1.0f / (m03 * ndcX + m13 * ndcY + m23 * ndcZ + m33)
    dest.x = (m00 * ndcX + m10 * ndcY + m20 * ndcZ + m30) * invW
    dest.y = (m01 * ndcX + m11 * ndcY + m21 * ndcZ + m31) * invW
    dest.z = (m02 * ndcX + m12 * ndcY + m22 * ndcZ + m32) * invW
    return dest
}

fun Matrix4fc.sub(subtrahend: Matrix4fc, dest: Matrix4f): Matrix4f {
    dest.m00 = m00 - subtrahend.m00
    dest.m01 = m01 - subtrahend.m01
    dest.m02 = m02 - subtrahend.m02
    dest.m03 = m03 - subtrahend.m03
    dest.m10 = m10 - subtrahend.m10
    dest.m11 = m11 - subtrahend.m11
    dest.m12 = m12 - subtrahend.m12
    dest.m13 = m13 - subtrahend.m13
    dest.m20 = m20 - subtrahend.m20
    dest.m21 = m21 - subtrahend.m21
    dest.m22 = m22 - subtrahend.m22
    dest.m23 = m23 - subtrahend.m23
    dest.m30 = m30 - subtrahend.m30
    dest.m31 = m31 - subtrahend.m31
    dest.m32 = m32 - subtrahend.m32
    dest.m33 = m33 - subtrahend.m33
    dest.properties = 0
    return dest
}

fun Matrix4fc.frustumCorner(corner: Int, point: Vector3fm): Vector3fm {
    val d1: Float
    val d2: Float
    val d3: Float
    val n1x: Float
    val n1y: Float
    val n1z: Float
    val n2x: Float
    val n2y: Float
    val n2z: Float
    val n3x: Float
    val n3y: Float
    val n3z: Float
    when (corner) {
        Matrix4fc.CORNER_NXNYNZ -> {
            n1x = m03 + m00
            n1y = m13 + m10
            n1z = m23 + m20
            d1 = m33 + m30 // left
            n2x = m03 + m01
            n2y = m13 + m11
            n2z = m23 + m21
            d2 = m33 + m31 // bottom
            n3x = m03 + m02
            n3y = m13 + m12
            n3z = m23 + m22
            d3 = m33 + m32 // near
        }
        Matrix4fc.CORNER_PXNYNZ -> {
            n1x = m03 - m00
            n1y = m13 - m10
            n1z = m23 - m20
            d1 = m33 - m30 // right
            n2x = m03 + m01
            n2y = m13 + m11
            n2z = m23 + m21
            d2 = m33 + m31 // bottom
            n3x = m03 + m02
            n3y = m13 + m12
            n3z = m23 + m22
            d3 = m33 + m32 // near
        }
        Matrix4fc.CORNER_PXPYNZ -> {
            n1x = m03 - m00
            n1y = m13 - m10
            n1z = m23 - m20
            d1 = m33 - m30 // right
            n2x = m03 - m01
            n2y = m13 - m11
            n2z = m23 - m21
            d2 = m33 - m31 // top
            n3x = m03 + m02
            n3y = m13 + m12
            n3z = m23 + m22
            d3 = m33 + m32 // near
        }
        Matrix4fc.CORNER_NXPYNZ -> {
            n1x = m03 + m00
            n1y = m13 + m10
            n1z = m23 + m20
            d1 = m33 + m30 // left
            n2x = m03 - m01
            n2y = m13 - m11
            n2z = m23 - m21
            d2 = m33 - m31 // top
            n3x = m03 + m02
            n3y = m13 + m12
            n3z = m23 + m22
            d3 = m33 + m32 // near
        }
        Matrix4fc.CORNER_PXNYPZ -> {
            n1x = m03 - m00
            n1y = m13 - m10
            n1z = m23 - m20
            d1 = m33 - m30 // right
            n2x = m03 + m01
            n2y = m13 + m11
            n2z = m23 + m21
            d2 = m33 + m31 // bottom
            n3x = m03 - m02
            n3y = m13 - m12
            n3z = m23 - m22
            d3 = m33 - m32 // far
        }
        Matrix4fc.CORNER_NXNYPZ -> {
            n1x = m03 + m00
            n1y = m13 + m10
            n1z = m23 + m20
            d1 = m33 + m30 // left
            n2x = m03 + m01
            n2y = m13 + m11
            n2z = m23 + m21
            d2 = m33 + m31 // bottom
            n3x = m03 - m02
            n3y = m13 - m12
            n3z = m23 - m22
            d3 = m33 - m32 // far
        }
        Matrix4fc.CORNER_NXPYPZ -> {
            n1x = m03 + m00
            n1y = m13 + m10
            n1z = m23 + m20
            d1 = m33 + m30 // left
            n2x = m03 - m01
            n2y = m13 - m11
            n2z = m23 - m21
            d2 = m33 - m31 // top
            n3x = m03 - m02
            n3y = m13 - m12
            n3z = m23 - m22
            d3 = m33 - m32 // far
        }
        Matrix4fc.CORNER_PXPYPZ -> {
            n1x = m03 - m00
            n1y = m13 - m10
            n1z = m23 - m20
            d1 = m33 - m30 // right
            n2x = m03 - m01
            n2y = m13 - m11
            n2z = m23 - m21
            d2 = m33 - m31 // top
            n3x = m03 - m02
            n3y = m13 - m12
            n3z = m23 - m22
            d3 = m33 - m32 // far
        }
        else -> throw IllegalArgumentException("corner") //$NON-NLS-1$
    }
    val c23x: Float
    val c23y: Float
    val c23z: Float
    c23x = n2y * n3z - n2z * n3y
    c23y = n2z * n3x - n2x * n3z
    c23z = n2x * n3y - n2y * n3x
    val c31x: Float
    val c31y: Float
    val c31z: Float
    c31x = n3y * n1z - n3z * n1y
    c31y = n3z * n1x - n3x * n1z
    c31z = n3x * n1y - n3y * n1x
    val c12x: Float
    val c12y: Float
    val c12z: Float
    c12x = n1y * n2z - n1z * n2y
    c12y = n1z * n2x - n1x * n2z
    c12z = n1x * n2y - n1y * n2x
    val invDot = 1.0f / (n1x * c23x + n1y * c23y + n1z * c23z)
    point.x = (-c23x * d1 - c31x * d2 - c12x * d3) * invDot
    point.y = (-c23y * d1 - c31y * d2 - c12y * d3) * invDot
    point.z = (-c23z * d1 - c31z * d2 - c12z * d3) * invDot
    return point
}

fun Matrix4f.setTranslation(translation: Vector3fc): Matrix4f = setTranslation(translation.x, translation.y, translation.z)

fun Matrix4f.setTranslation(x: Float, y: Float, z: Float): Matrix4f {
    this.m30 = x
    this.m31 = y
    this.m32 = z
    properties = properties and (PROPERTY_PERSPECTIVE or PROPERTY_IDENTITY).inv()
    return this
}

/**
 * Set only the upper left 3x3 submatrix of this matrix to a rotation of `angleX` radians about the X axis, followed by a rotation
 * of `angleY` radians about the Y axis and followed by a rotation of `angleZ` radians about the Z axis.
 *
 *
 * When used with a right-handed coordinate system, the produced rotation will rotate a vector
 * counter-clockwise around the rotation axis, when viewing along the negative axis direction towards the origin.
 * When used with a left-handed coordinate system, the rotation is clockwise.
 *
 * @param angleX
 * the angle to rotate about X
 * @param angleY
 * the angle to rotate about Y
 * @param angleZ
 * the angle to rotate about Z
 * @return this
 */
fun Matrix4f.setRotationXYZ(angleX: Float, angleY: Float, angleZ: Float): Matrix4f {
    val sinX = sin(angleX)
    val cosX = cosFromSin(sinX, angleX)
    val sinY = sin(angleY)
    val cosY = cosFromSin(sinY, angleY)
    val sinZ = sin(angleZ)
    val cosZ = cosFromSin(sinZ, angleZ)
    val m_sinX = -sinX
    val m_sinY = -sinY
    val m_sinZ = -sinZ
    // rotateX
    // rotateY
    val nm01 = m_sinX * m_sinY
    val nm02 = cosX * m_sinY
    this.m20 = (sinY)
    this.m21 = (m_sinX * cosY)
    this.m22 = (cosX * cosY)
    // rotateZ
    this.m00 = cosY * cosZ
    this.m01 = nm01 * cosZ + cosX * sinZ
    this.m02 = nm02 * cosZ + sinX * sinZ
    this.m10 = cosY * m_sinZ
    this.m11 = nm01 * m_sinZ + cosX * cosZ
    this.m12 = nm02 * m_sinZ + sinX * cosZ
    properties = properties and (PROPERTY_PERSPECTIVE or PROPERTY_IDENTITY or PROPERTY_TRANSLATION).inv()
    return this
}

fun Matrix4fc.getAxisAngleRotation(dest:Vector4fm): Vector4fm {
    val m = this
    var nm00= m.m00
    var nm01= m.m01
    var nm02 = m.m02
    var nm10 = m.m10
    var nm11 = m.m11
    var nm12 = m.m12
    var nm20 = m.m20
    var nm21 = m.m21
    var nm22 = m.m22
    val lenX = 1.0f / sqrt(m.m00 * m.m00 + m.m01 * m.m01 + (m.m02 * m.m02))
    val lenY = 1.0f / sqrt(m.m10 * m.m10 + m.m11 * m.m11 + (m.m12 * m.m12))
    val lenZ = 1.0f / sqrt(m.m20 * m.m20 + m.m21 * m.m21 + (m.m22 * m.m22))
    nm00 *= lenX
    nm01 *= lenX
    nm02 *= lenX
    nm10 *= lenY
    nm11 *= lenY
    nm12 *= lenY
    nm20 *= lenZ
    nm21 *= lenZ
    nm22 *= lenZ
    val epsilon = 1E-4
    if (abs(nm10 - nm01) < epsilon && abs(nm20 - nm02) < epsilon && abs(nm21 - nm12) < epsilon) {
        dest.w = PIf
        val xx = (nm00 + 1) / 2
        val yy = (nm11 + 1) / 2
        val zz = (nm22 + 1) / 2
        val xy = (nm10 + nm01) / 4
        val xz = (nm20 + nm02) / 4
        val yz = (nm21 + nm12) / 4
        if (xx > yy && xx > zz) {
            dest.x = sqrt(xx)
            dest.y = (xy / dest.x)
            dest.z = (xz / dest.x)
        } else if (yy > zz) {
            dest.y = sqrt(yy)
            dest.x = (xy / dest.y)
            dest.z = (yz / dest.y)
        } else {
            dest.z = sqrt(zz)
            dest.x = (xz / dest.z)
            dest.y = (yz / dest.z)
        }
        return dest
    }
    val s = sqrt((nm12 - nm21) * (nm12 - nm21) + (nm20 - nm02) * (nm20 - nm02) + (nm01 - nm10) * (nm01 - nm10))
    dest.w = safeAcos((nm00 + nm11 + nm22 - 1) / 2)
    dest.x = ((nm12 - nm21) / s)
    dest.y = ((nm20 - nm02) / s)
    dest.z = ((nm01 - nm10) / s)
    return dest
}