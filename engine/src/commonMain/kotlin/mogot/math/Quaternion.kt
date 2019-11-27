package mogot.math

import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sin
import kotlin.math.sqrt

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

    fun rotateZYX(angleZ: Float, angleY: Float, angleX: Float, dest: Quaternionf): Quaternionf {
        val sx = sin(angleX * 0.5).toFloat()
        val cx = cosFromSin(sx.toDouble(), angleX * 0.5).toFloat()
        val sy = sin(angleY * 0.5).toFloat()
        val cy = cosFromSin(sy.toDouble(), angleY * 0.5).toFloat()
        val sz = sin(angleZ * 0.5).toFloat()
        val cz = cosFromSin(sz.toDouble(), angleZ * 0.5).toFloat()
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

    fun slerp(target: Quaternionfc, alpha: Float, dest: Quaternionf): Quaternionf {
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

    fun positiveX(dir: Vector3f): Vector3f {
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

    fun positiveY(dir: Vector3f): Vector3f {
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

    fun positiveZ(dir: Vector3f): Vector3f {
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

    fun rotateXYZ(angleX: Float, angleY: Float, angleZ: Float, dest: Quaternionf): Quaternionf {
        val sx = sin(angleX * 0.5).toFloat()
        val cx = cosFromSin(sx.toDouble(), angleX * 0.5).toFloat()
        val sy = sin(angleY * 0.5).toFloat()
        val cy = cosFromSin(sy.toDouble(), angleY * 0.5).toFloat()
        val sz = sin(angleZ * 0.5).toFloat()
        val cz = cosFromSin(sz.toDouble(), angleZ * 0.5).toFloat()
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

    fun rotateAxis(angle: Float, axisX: Float, axisY: Float, axisZ: Float, dest: Quaternionf): Quaternionf? {
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
}

class Quaternionf(override var x: Float = 0f, override var y: Float = 0f, override var z: Float = 0f, override var w: Float = 1f) : Quaternionfc {
    fun identity(): Quaternionf {
        x = 0f
        y = 0f
        z = 0f
        w = 1f
        return this
    }

    fun rotateAxis(angle: Float, axisX: Float, axisY: Float, axisZ: Float): Quaternionf? =
            rotateAxis(angle, axisX, axisY, axisZ, this)

    override fun toString(): String =
            "Quaternionf($x, $y, $z, $w)"

    fun rotateZYX(angleZ: Float, angleY: Float, angleX: Float): Quaternionf =
            rotateZYX(angleZ, angleY, angleX, this)

    fun slerp(target: Quaternionfc, alpha: Float): Quaternionf = slerp(target, alpha, this)

    fun set(x: Float, y: Float, z: Float, w: Float) {
        this.x = x
        this.y = y
        this.z = z
        this.w = w
    }

    fun lookAlong(dir: Vector3fc, up: Vector3fc, dest: Quaternionf): Quaternionf {
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

    fun lookAlong(dir: Vector3fc, up: Vector3fc): Quaternionf = lookAlong(dir, up, this)

    fun rotateXYZ(angleX: Float, angleY: Float, angleZ: Float): Quaternionf =
            rotateXYZ(angleX, angleY, angleZ, this)
}


val Quaternionfc.right: Vector3f
    get() = positiveX(Vector3f())

val Quaternionfc.left: Vector3f
    get() = right.negate()

val Quaternionfc.up: Vector3f
    get() = positiveY(Vector3f())

val Quaternionfc.forward: Vector3f
    get() = positiveZ(Vector3f()).negate()