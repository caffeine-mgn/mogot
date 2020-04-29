package mogot.math

import kotlin.math.*


/*
expect interface Quaternionfc{
    fun x():Float
    fun y():Float
    fun z():Float
    fun w():Float
}
expect class Quaternionf : Quaternionfc {
    constructor()

    fun identity(): Quaternionf
    fun lookAlong(dir: Vector3fc, up: Vector3fc):Quaternionf
}*/

interface Quaternionfc {
    val x: Float
    val y: Float
    val z: Float
    val w: Float

    companion object

    fun lookAlong(dir: Vector3fc, up: Vector3fc, dest: Quaternionfm): Quaternionfm {
        // Normalize direction
        val invDirLength = (1.0f / sqrt(dir.x * dir.x + dir.y * dir.y + dir.z * dir.z))
        val dirnX = -dir.x * invDirLength
        val dirnY = -dir.y * invDirLength
        val dirnZ = -dir.z * invDirLength
        // left = up x dir
        var leftX = up.y * dirnZ - up.z * dirnY
        var leftY = up.z * dirnX - up.x * dirnZ
        var leftZ = up.x * dirnY - up.y * dirnX
        // normalize left
        val invLeftLength = (1.0 / sqrt(leftX * leftX + leftY * leftY + leftZ * leftZ)).toFloat()
        leftX *= invLeftLength;
        leftY *= invLeftLength;
        leftZ *= invLeftLength;
        // up = direction x left
        val upnX = dirnY * leftZ - dirnZ * leftY
        val upnY = dirnZ * leftX - dirnX * leftZ
        val upnZ = dirnX * leftY - dirnY * leftX

        /* Convert orthonormal basis vectors to quaternion */
        var x: Float
        var y: Float
        var z: Float
        var w: Float
        var t: Float
        var tr = leftX + upnY + dirnZ;
        if (tr >= 0.0) {
            t = sqrt(tr + 1.0f)
            w = (t * 0.5f)
            t = 0.5f / t
            x = ((dirnY - upnZ) * t)
            y = ((leftZ - dirnX) * t)
            z = ((upnX - leftY) * t)
        } else {
            if (leftX > upnY && leftX > dirnZ) {
                t = sqrt(1.0f + leftX - upnY - dirnZ);
                x = (t * 0.5).toFloat()
                t = 0.5f / t;
                y = ((leftY + upnX) * t)
                z = ((dirnX + leftZ) * t)
                w = ((dirnY - upnZ) * t)
            } else if (upnY > dirnZ) {
                t = sqrt(1.0f + upnY - leftX - dirnZ);
                y = (t * 0.5).toFloat()
                t = 0.5f / t
                x = ((leftY + upnX) * t)
                z = ((upnZ + dirnY) * t)
                w = ((leftZ - dirnX) * t)
            } else {
                t = sqrt(1.0f + dirnZ - leftX - upnY);
                z = (t * 0.5f)
                t = 0.5f / t
                x = ((dirnX + leftZ) * t)
                y = ((upnZ + dirnY) * t)
                w = ((upnX - leftY) * t)
            }
        }
//        dest.set(x, y, z, w)
        /* Multiply */

        dest.set(this.w * x + this.x * w + this.y * z - this.z * y,
                this.w * y - this.x * z + this.y * w + this.z * x,
                this.w * z + this.x * y - this.y * x + this.z * w,
                this.w * w - this.x * x - this.y * y - this.z * z)
        return dest
    }

    fun rotateZYX(angleZ: Float, angleY: Float, angleX: Float, dest: Quaternionfm): Quaternionfm {
        val sx = sin(angleX * 0.5f)
        val cx = cosFromSin(sx, angleX * 0.5f)
        val sy = sin(angleY * 0.5f)
        val cy = cosFromSin(sy, angleY * 0.5f)
        val sz = sin(angleZ * 0.5f)
        val cz = cosFromSin(sz, angleZ * 0.5f)
        val cycz = cy * cz
        val sysz = sy * sz
        val sycz = sy * cz
        val cysz = cy * sz
        val w = cx * cycz + sx * sysz
        val x = sx * cycz - cx * sysz
        val y = cx * sycz + sx * cysz
        val z = cx * cysz - sx * sycz
        // right-multiply
        dest.set(this.w * x + this.x * w + this.y * z - this.z * y,
                this.w * y - this.x * z + this.y * w + this.z * x,
                this.w * z + this.x * y - this.y * x + this.z * w,
                this.w * w - this.x * x - this.y * y - this.z * z)
        return dest
    }

    fun slerp(target: Quaternionfc, alpha: Float, dest: Quaternionfm): Quaternionfm {
        val cosom: Float = x * target.x + y * target.y + z * target.z + w * target.w
        val absCosom: Float = abs(cosom)
        val scale0: Float
        var scale1: Float
        if (1.0f - absCosom > 1E-6f) {
            val sinSqr = 1.0f - absCosom * absCosom
            val sinom = (1.0 / sqrt(sinSqr.toDouble())).toFloat()
            val omega = atan2(sinSqr * sinom.toDouble(), absCosom.toDouble()).toFloat()
            scale0 = (sin((1.0 - alpha) * omega) * sinom).toFloat()
            scale1 = (sin(alpha * omega.toDouble()) * sinom).toFloat()
        } else {
            scale0 = 1.0f - alpha
            scale1 = alpha
        }
        scale1 = if (cosom >= 0.0f) scale1 else -scale1
        dest.x = scale0 * x + scale1 * target.x
        dest.y = scale0 * y + scale1 * target.y
        dest.z = scale0 * z + scale1 * target.z
        dest.w = scale0 * w + scale1 * target.w
        return dest
    }

    fun positiveX(dir: Vector3fm): Vector3fm {
        val invNorm = 1.0f / (x * x + y * y + z * z + w * w)
        val nx = -x * invNorm
        val ny = -y * invNorm
        val nz = -z * invNorm
        val nw = w * invNorm
        val dy = ny + ny
        val dz = nz + nz
        dir.x = -ny * dy - nz * dz + 1.0f
        dir.y = nx * dy + nw * dz
        dir.z = nx * dz - nw * dy
        return dir
    }

    fun positiveY(dir: Vector3fm): Vector3fm {
        val invNorm = 1.0f / (x * x + y * y + z * z + w * w)
        val nx = -x * invNorm
        val ny = -y * invNorm
        val nz = -z * invNorm
        val nw = w * invNorm
        val dx = nx + nx
        val dy = ny + ny
        val dz = nz + nz
        dir.x = nx * dy - nw * dz
        dir.y = -nx * dx - nz * dz + 1.0f
        dir.z = ny * dz + nw * dx
        return dir
    }

    fun positiveZ(dir: Vector3fm): Vector3fm {
        val invNorm = 1.0f / (x * x + y * y + z * z + w * w)
        val nx = -x * invNorm
        val ny = -y * invNorm
        val nz = -z * invNorm
        val nw = w * invNorm
        val dx = nx + nx
        val dy = ny + ny
        val dz = nz + nz
        dir.x = nx * dz + nw * dy
        dir.y = ny * dz - nw * dx
        dir.z = -nx * dx - ny * dy + 1.0f
        return dir
    }

    fun rotateXYZ(angleX: Float, angleY: Float, angleZ: Float, dest: Quaternionfm): Quaternionfm {
        val sx = sin(angleX * 0.5f)
        val cx = cosFromSin(sx, angleX * 0.5f)
        val sy = sin(angleY * 0.5f)
        val cy = cosFromSin(sy, angleY * 0.5f)
        val sz = sin(angleZ * 0.5f)
        val cz = cosFromSin(sz, angleZ * 0.5f)
        val cycz = cy * cz
        val sysz = sy * sz
        val sycz = sy * cz
        val cysz = cy * sz
        val w = cx * cycz - sx * sysz
        val x = sx * cycz + cx * sysz
        val y = cx * sycz - sx * cysz
        val z = cx * cysz + sx * sycz
        // right-multiply
        dest.set(this.w * x + this.x * w + this.y * z - this.z * y,
                this.w * y - this.x * z + this.y * w + this.z * x,
                this.w * z + this.x * y - this.y * x + this.z * w,
                this.w * w - this.x * x - this.y * y - this.z * z)
        return dest
    }

    fun rotateAxis(angle: Float, axisX: Float, axisY: Float, axisZ: Float, dest: Quaternionfm): Quaternionfm? {
        val hangle = angle / 2.0f
        val sinAngle = sin(hangle)
        val invVLength = 1.0f / sqrt(axisX * axisX + axisY * axisY + (axisZ * axisZ))
        val rx = axisX * invVLength * sinAngle
        val ry = axisY * invVLength * sinAngle
        val rz = axisZ * invVLength * sinAngle
        val rw = cosFromSin(sinAngle, hangle)
        dest.set((w * rx + x * rw + y * rz - z * ry),
                (w * ry - x * rz + y * rw + z * rx),
                (w * rz + x * ry - y * rx + z * rw),
                (w * rw - x * rx - y * ry - z * rz))
        return dest
    }

    fun getEulerAnglesXYZ(eulerAngles: Vector3fm): Vector3fm {
        eulerAngles.x = atan2(2.0f * (x * w - y * z), 1.0f - 2.0f * (x * x + y * y))
        eulerAngles.y = asin(2.0f * (x * z + y * w))
        eulerAngles.z = atan2(2.0f * (z * w - x * y), 1.0f - 2.0f * (y * y + z * z))
        return eulerAngles
    }
}

fun Quaternionfm.set(quaternion: Quaternionfc) = set(quaternion.x, quaternion.y, quaternion.z, quaternion.w)

interface Quaternionfm : Quaternionfc {
    override var x: Float
    override var y: Float
    override var z: Float
    override var w: Float

    fun set(x: Float, y: Float, z: Float, w: Float) {
        this.x = x
        this.y = y
        this.z = z
        this.w = w
    }

    fun lookAlong(dir: Vector3fc, up: Vector3fc) = lookAlong(dir, up, this)

    fun rotateXYZ(angleX: Float, angleY: Float, angleZ: Float) =
            rotateXYZ(angleX, angleY, angleZ, this)

    fun rotateAxis(angle: Float, axisX: Float, axisY: Float, axisZ: Float) =
            rotateAxis(angle, axisX, axisY, axisZ, this)

    fun rotateZYX(angleZ: Float, angleY: Float, angleX: Float) =
            rotateZYX(angleZ, angleY, angleX, this)

    fun slerp(target: Quaternionfc, alpha: Float) = slerp(target, alpha, this)

    fun identity(): Quaternionfm {
        x = 0f
        y = 0f
        z = 0f
        w = 1f
        return this
    }
}

fun Quaternionfc.invert(dest: Quaternionfm): Quaternionfm {
    val invNorm: Float = 1.0f / (x * x + y * y + z * z + w * w)
    dest.x = -x * invNorm
    dest.y = -y * invNorm
    dest.z = -z * invNorm
    dest.w = w * invNorm
    return dest
}

fun Quaternionfm.invert() = invert(this)

operator fun Quaternionfc.unaryMinus() = invert(Quaternionf())

open class Quaternionf(override var x: Float = 0f, override var y: Float = 0f, override var z: Float = 0f, override var w: Float = 1f) : Quaternionfm {
    constructor(quaternion: Quaternionfc) : this(quaternion.x, quaternion.y, quaternion.z, quaternion.w)

    override fun toString(): String =
            "Quaternionf($x, $y, $z, $w)"
}


val Quaternionfc.right: Vector3fc
    get() = positiveX(Vector3f())

val Quaternionfc.left: Vector3fc
    get() = right.negated()

val Quaternionfc.up: Vector3fc
    get() = positiveY(Vector3f())

val Quaternionfc.forward: Vector3fc
    get() = positiveY(Vector3f()).negate()


val Quaternionfc.roll: Float
    get(){
        val sinr_cosp = 2f * (w * x + y * z)
        val cosr_cosp = 1f - 2f * (x * x + y * y)
        return atan2(sinr_cosp, cosr_cosp)
    }// = atan2(2.0f * (y * z + w * x), (w * w - x * x - y * y + z * z))//atan2(2*y*w - 2*x*z, 1 - 2*y*y - 2*z*z)

val Quaternionfc.pitch: Float
    get() {
        val sinp = 2f * (w * y - z * x)
        return if (sinp >= 1f)
            PIHalf
        else
            asin(sinp)
    }//atan2(2*x*w + 2*y*z, 1 - 2*x*x - 2*z*z)//asin(-2.0f * (x * z - w * y))//atan2(2*x*w - 2*y*z, 1 - 2*x*x - 2*z*z)

val Quaternionfc.yaw: Float
    get(){
        val siny_cosp = 2f * (w * z + x * y)
        val cosy_cosp = 1f - 2f * (y * y + z * z)
        return atan2(siny_cosp, cosy_cosp)
    }// = atan2(2.0f * (x * y + w * z), (w * w + x * x - y * y - z * z))//asin(2*x*y + 2*z*w)

fun Quaternionfm.setRotation(yaw: Float = this.yaw, pitch: Float = this.pitch, roll: Float = this.roll): Quaternionfm =
        setRotation(yaw, pitch, roll, this)

class RotationVector(val quaternion: Quaternionfm) : Vector3fm {
    override var x: Float
        get() = quaternion.roll
        set(value) {
            quaternion.setRotation(z, y, value)
        }
    override var y: Float
        get() = quaternion.pitch
        set(value) {
            quaternion.setRotation(z, value, x)
        }
    override var z: Float
        get() = quaternion.yaw
        set(value) {
            quaternion.setRotation(value, y, x)
        }

    override fun set(x: Float, y: Float, z: Float): Vector3fm {
        quaternion.setRotation(z, y, x)
        return this
    }

    override fun set(value: Float): Vector3fm {
        quaternion.setRotation(value, value, value)
        return this
    }
}

fun Quaternionfm.mul(q: Quaternionfm) = mul(q, q)

fun Quaternionfc.mul(q: Quaternionfc, dest: Quaternionfm): Quaternionfm {
    dest.set(w * q.x + x * q.w + y * q.z - z * q.y,
            w * q.y - x * q.z + y * q.w + z * q.x,
            w * q.z + x * q.y - y * q.x + z * q.w,
            w * q.w - x * q.x - y * q.y - z * q.z)
    return dest
}

fun Quaternionfc.mul(vector: Vector3fm) = mul(vector, vector)

fun Quaternionfc.mul(vector: Vector3fc, dest: Vector3fm): Vector3fm {
    val tempX: Float = w * w * vector.x + 2 * y * w * vector.z - 2 * z * w * vector.y + x * x * vector.x + 2 * y * x * vector.y + 2 * z * x * vector.z - z * z * vector.x - y * y * vector.x
    val tempY: Float = 2 * x * y * vector.x + y * y * vector.y + 2 * z * y * vector.z + (2 * w * z * vector.x) - z * z * vector.y + w * w * vector.y - 2 * x * w * vector.z - x * x * vector.y
    val tempZ: Float = 2 * x * z * vector.x + 2 * y * z * vector.y + z * z * vector.z - (2 * w * y * vector.x) - y * y * vector.z + 2 * w * x * vector.y - x * x * vector.z + w * w * vector.z
    dest.set(tempX, tempY, tempZ)
    return dest
}

// yaw (Z), pitch (Y), roll (X)
fun Quaternionfc.setRotation(yaw: Float, pitch: Float, roll: Float, dest: Quaternionfm): Quaternionfm {
    // Abbreviations for the various angular functions
    val cy = cos(yaw * 0.5f)
    val sy = sin(yaw * 0.5f)
    val cp = cos(pitch * 0.5f)
    val sp = sin(pitch * 0.5f)
    val cr = cos(roll * 0.5f)
    val sr = sin(roll * 0.5f)

    dest.w = (cy * cp * cr + sy * sp * sr)
    dest.x = (cy * cp * sr - sy * sp * cr)
    dest.y = (sy * cp * sr + cy * sp * cr)
    dest.z = (sy * cp * cr - cy * sp * sr)
    return dest
}

operator fun Quaternionfc.times(other: Quaternionfm) = mul(other, Quaternionf())


fun Quaternionfm.setFromUnnormalized(mat: Matrix4fc): Quaternionfm {
    setFromUnnormalized(mat.m00, mat.m01, mat.m02, mat.m10, mat.m11, mat.m12, mat.m20, mat.m21, mat.m22)
    return this
}

fun Quaternionfm.setFromUnnormalized(m00: Float, m01: Float, m02: Float, m10: Float, m11: Float, m12: Float, m20: Float, m21: Float, m22: Float) {
    var nm00 = m00
    var nm01 = m01
    var nm02 = m02
    var nm10 = m10
    var nm11 = m11
    var nm12 = m12
    var nm20 = m20
    var nm21 = m21
    var nm22 = m22
    val lenX = (1.0 / sqrt(m00 * m00 + m01 * m01 + (m02 * m02).toDouble())).toFloat()
    val lenY = (1.0 / sqrt(m10 * m10 + m11 * m11 + (m12 * m12).toDouble())).toFloat()
    val lenZ = (1.0 / sqrt(m20 * m20 + m21 * m21 + (m22 * m22).toDouble())).toFloat()
    nm00 *= lenX
    nm01 *= lenX
    nm02 *= lenX
    nm10 *= lenY
    nm11 *= lenY
    nm12 *= lenY
    nm20 *= lenZ
    nm21 *= lenZ
    nm22 *= lenZ
    setFromNormalized(nm00, nm01, nm02, nm10, nm11, nm12, nm20, nm21, nm22)
}

fun Quaternionfm.setFromNormalized(m00: Float, m01: Float, m02: Float, m10: Float, m11: Float, m12: Float, m20: Float, m21: Float, m22: Float) {
    var t: Float
    val tr = m00 + m11 + m22
    if (tr >= 0.0f) {
        t = sqrt(tr + 1.0f)
        w = t * 0.5f
        t = 0.5f / t
        x = (m12 - m21) * t
        y = (m20 - m02) * t
        z = (m01 - m10) * t
    } else {
        if (m00 >= m11 && m00 >= m22) {
            t = sqrt(m00 - (m11 + m22) + 1.0f)
            x = t * 0.5f
            t = 0.5f / t
            y = (m10 + m01) * t
            z = (m02 + m20) * t
            w = (m12 - m21) * t
        } else if (m11 > m22) {
            t = sqrt(m11 - (m22 + m00) + 1.0f)
            y = t * 0.5f
            t = 0.5f / t
            z = (m21 + m12) * t
            x = (m10 + m01) * t
            w = (m20 - m02) * t
        } else {
            t = sqrt(m22 - (m00 + m11) + 1.0f)
            z = t * 0.5f
            t = 0.5f / t
            x = (m02 + m20) * t
            y = (m21 + m12) * t
            w = (m01 - m10) * t
        }
    }
}

fun Quaternionfm.lookAt(direction: Vector3fc, up: Vector3fc): Quaternionfm {
    val direction = direction.negated()
    val vect3 = direction.normalize(Vector3f())
    val vect1 = up.cross(direction, Vector3f()).normalize()
    val vect2 = direction.cross(vect1, Vector3f()).normalize()
    fromAxes(vect1, vect2, vect3)
    return this
}

private fun CreateFromAxisAngle(axis: Vector3fc, angle: Float, dest: Quaternionfm): Quaternionfm {
    val halfAngle = angle * 0.5f;
    val s = sin(halfAngle)

    dest.x = axis.x * s;
    dest.y = axis.y * s;
    dest.z = axis.z * s;
    dest.w = cos(halfAngle)
    return dest;
}

/*
fun Quaternionfm.LookAt(destPoint: Vector3fc): Quaternionfm {
        /*Vector forward = lookAt.Normalized();
        Vector right = Vector::Cross(up.Normalized(), forward);
        Vector up = Vector::Cross(forward, right);*/

        Vector forward = lookAt.Normalized();
        Vector::OrthoNormalize(&up, &forward); // Keeps up the same, make forward orthogonal to up
        Vector right = Vector::Cross(up, forward);

        Quaternion ret;
        ret.w = sqrtf(1.0f + right.x + up.y + forward.z) * 0.5f;
        float w4_recip = 1.0f / (4.0f * ret.w);
        ret.x = (forward.y - up.z) * w4_recip;
        ret.y = (right.z - forward.x) * w4_recip;
        ret.z = (up.x - right.y) * w4_recip;

        return ret;
}
*/

fun Quaternionfm.fromAxes(xAxis: Vector3fc, yAxis: Vector3fc, zAxis: Vector3fc): Quaternionfm {
    return fromRotationMatrix(xAxis.x, yAxis.x, zAxis.x, xAxis.y, yAxis.y,
            zAxis.y, xAxis.z, yAxis.z, zAxis.z)
}

fun Quaternionfm.fromRotationMatrix(m00: Float, m01: Float, m02: Float,
                                    m10: Float, m11: Float, m12: Float, m20: Float, m21: Float, m22: Float): Quaternionfm { // first normalize the forward (F), up (U) and side (S) vectors of the rotation matrix
// so that the scale does not affect the rotation
    var m00 = m00
    var m01 = m01
    var m02 = m02
    var m10 = m10
    var m11 = m11
    var m12 = m12
    var m20 = m20
    var m21 = m21
    var m22 = m22
    var lengthSquared = m00 * m00 + m10 * m10 + m20 * m20
    if (lengthSquared != 1f && lengthSquared != 0f) {
        lengthSquared = 1.0f / sqrt(lengthSquared)
        m00 *= lengthSquared
        m10 *= lengthSquared
        m20 *= lengthSquared
    }
    lengthSquared = m01 * m01 + m11 * m11 + m21 * m21
    if (lengthSquared != 1f && lengthSquared != 0f) {
        lengthSquared = 1.0f / sqrt(lengthSquared)
        m01 *= lengthSquared
        m11 *= lengthSquared
        m21 *= lengthSquared
    }
    lengthSquared = m02 * m02 + m12 * m12 + m22 * m22
    if (lengthSquared != 1f && lengthSquared != 0f) {
        lengthSquared = 1.0f / sqrt(lengthSquared)
        m02 *= lengthSquared
        m12 *= lengthSquared
        m22 *= lengthSquared
    }
    // Use the Graphics Gems code, from
// ftp://ftp.cis.upenn.edu/pub/graphics/shoemake/quatut.ps.Z
// *NOT* the "Matrix and Quaternions FAQ", which has errors!
// the trace is the sum of the diagonal elements; see
// http://mathworld.wolfram.com/MatrixTrace.html
    val t = m00 + m11 + m22
    // we protect the division by s by ensuring that s>=1
    if (t >= 0) { // |w| >= .5
        var s: Float = sqrt(t + 1) // |s|>=1 ...
        w = 0.5f * s
        s = 0.5f / s // so this division isn't bad
        x = (m21 - m12) * s
        y = (m02 - m20) * s
        z = (m10 - m01) * s
    } else if (m00 > m11 && m00 > m22) {
        var s: Float = sqrt(1.0f + m00 - m11 - m22) // |s|>=1
        x = s * 0.5f // |x| >= .5
        s = 0.5f / s
        y = (m10 + m01) * s
        z = (m02 + m20) * s
        w = (m21 - m12) * s
    } else if (m11 > m22) {
        var s: Float = sqrt(1.0f + m11 - m00 - m22) // |s|>=1
        y = s * 0.5f // |y| >= .5
        s = 0.5f / s
        x = (m10 + m01) * s
        z = (m21 + m12) * s
        w = (m02 - m20) * s
    } else {
        var s: Float = sqrt(1.0f + m22 - m00 - m11) // |s|>=1
        z = s * 0.5f // |z| >= .5
        s = 0.5f / s
        x = (m02 + m20) * s
        y = (m21 + m12) * s
        w = (m10 - m01) * s
    }
    return this
}

fun Quaternionfm.normalize(): Quaternionfm {
    val invNorm = (1.0f / sqrt(x * x + y * y + z * z + w * w))
    x *= invNorm
    y *= invNorm
    z *= invNorm
    w *= invNorm
    return this
}

fun Quaternionfm.rotationZYX(angleZ: Float, angleY: Float, angleX: Float): Quaternionfm {
    val sx = sin(angleX * 0.5f)
    val cx = cosFromSin(sx, angleX * 0.5f)
    val sy = sin(angleY * 0.5f)
    val cy = cosFromSin(sy, angleY * 0.5f)
    val sz = sin(angleZ * 0.5f)
    val cz = cosFromSin(sz, angleZ * 0.5f)
    val cycz = cy * cz
    val sysz = sy * sz
    val sycz = sy * cz
    val cysz = cy * sz
    w = cx * cycz + sx * sysz
    x = sx * cycz - cx * sysz
    y = cx * sycz + sx * cysz
    z = cx * cysz - sx * sycz
    return this
}