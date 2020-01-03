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

    fun lookAlong(dir: Vector3fc, up: Vector3fc, dest: Quaternionfm): Quaternionfm {
        // Normalize direction
        val invDirLength = (1.0 / kotlin.math.sqrt(dir.x * dir.x + dir.y * dir.y + dir.z * dir.z)).toFloat()
        val dirnX = -dir.x * invDirLength;
        val dirnY = -dir.y * invDirLength;
        val dirnZ = -dir.z * invDirLength;
        // left = up x dir
        var leftX = up.y * dirnZ - up.z * dirnY;
        var leftY = up.z * dirnX - up.x * dirnZ;
        var leftZ = up.x * dirnY - up.y * dirnX;
        // normalize left
        val invLeftLength = (1.0 / kotlin.math.sqrt(leftX * leftX + leftY * leftY + leftZ * leftZ)).toFloat()
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
        var t: Double
        var tr = leftX + upnY + dirnZ;
        if (tr >= 0.0) {
            t = kotlin.math.sqrt(tr + 1.0);
            w = (t * 0.5).toFloat()
            t = 0.5 / t;
            x = ((dirnY - upnZ) * t).toFloat()
            y = ((leftZ - dirnX) * t).toFloat()
            z = ((upnX - leftY) * t).toFloat()
        } else {
            if (leftX > upnY && leftX > dirnZ) {
                t = kotlin.math.sqrt(1.0 + leftX - upnY - dirnZ);
                x = (t * 0.5).toFloat()
                t = 0.5 / t;
                y = ((leftY + upnX) * t).toFloat()
                z = ((dirnX + leftZ) * t).toFloat()
                w = ((dirnY - upnZ) * t).toFloat()
            } else if (upnY > dirnZ) {
                t = kotlin.math.sqrt(1.0 + upnY - leftX - dirnZ);
                y = (t * 0.5).toFloat()
                t = 0.5 / t;
                x = ((leftY + upnX) * t).toFloat()
                z = ((upnZ + dirnY) * t).toFloat()
                w = ((leftZ - dirnX) * t).toFloat()
            } else {
                t = kotlin.math.sqrt(1.0 + dirnZ - leftX - upnY);
                z = (t * 0.5).toFloat()
                t = 0.5 / t;
                x = ((dirnX + leftZ) * t).toFloat()
                y = ((upnZ + dirnY) * t).toFloat()
                w = ((upnX - leftY) * t).toFloat()
            }
        }
        /* Multiply */
        dest.set(this.w * x + this.x * w + this.y * z - this.z * y,
                this.w * y - this.x * z + this.y * w + this.z * x,
                this.w * z + this.x * y - this.y * x + this.z * w,
                this.w * w - this.x * x - this.y * y - this.z * z);
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

open class Quaternionf(override var x: Float = 0f, override var y: Float = 0f, override var z: Float = 0f, override var w: Float = 1f) : Quaternionfm {
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
    get() = positiveZ(Vector3f()).negate()


val Quaternionfc.yaw: Float
    get() = atan2(2.0f * (y * z + w * x), (w * w - x * x - y * y + z * z))

val Quaternionfc.pitch: Float
    get() = asin(-2.0f * (x * z - w * y))

val Quaternionfc.roll: Float
    get() = atan2(2.0f * (x * y + w * z), (w * w + x * x - y * y - z * z))

// yaw (Z), pitch (Y), roll (X)
fun Quaternionfc.setRotation(yaw: Float, pitch: Float, roll: Float, dest: Quaternionf): Quaternionf {
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

fun Quaternionf.setFromUnnormalized(mat: Matrix4fc): Quaternionf {
    setFromUnnormalized(mat.m00, mat.m01, mat.m02, mat.m10, mat.m11, mat.m12, mat.m20, mat.m21, mat.m22)
    return this
}

fun Quaternionf.setFromUnnormalized(m00: Float, m01: Float, m02: Float, m10: Float, m11: Float, m12: Float, m20: Float, m21: Float, m22: Float) {
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

fun Quaternionf.setFromNormalized(m00: Float, m01: Float, m02: Float, m10: Float, m11: Float, m12: Float, m20: Float, m21: Float, m22: Float) {
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