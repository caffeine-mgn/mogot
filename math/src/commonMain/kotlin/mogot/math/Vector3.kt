@file:JvmName("Vec3KKT")

package mogot.math

import kotlin.jvm.JvmName
import kotlin.math.abs
import kotlin.math.sqrt

interface Vector3fc {
    val x: Float

    val y: Float

    val z: Float

    fun lerp(other: Vector3fc, t: Float, dest: Vector3fm): Vector3fm {
        dest.x = x + (other.x - x) * t
        dest.y = y + (other.y - y) * t
        dest.z = z + (other.z - z) * t
        return dest
    }

    fun normalize(dest: Vector3fm): Vector3fm {
        val lengthSquared = lengthSquared
        if (lengthSquared == 1f || lengthSquared == 0f)
            return dest
        val invLength: Float = 1.0f / sqrt(lengthSquared)
        dest.x = x * invLength
        dest.y = y * invLength
        dest.z = z * invLength
        return dest
    }

    val length
        get() = sqrt(lengthSquared)

    val lengthSquared
        get() = x * x + y * y + z * z

    fun copy() = Vector3f(x, y, z)

    fun negated(dest: Vector3fm) = dest.set(-x, -y, -z)
    fun negated() = negated(Vector3f())


    companion object {
        val X: Vector3fc = Vector3f(1f, 0f, 0f)
        val Y: Vector3fc = Vector3f(0f, 1f, 0f)
        val Z: Vector3fc = Vector3f(0f, 0f, 1f)
        val ZERO: Vector3fc = Vector3f(0f, 0f, 0f)

        inline val UP
            get() = Y

        inline val FORWARD
            get() = Z

        inline val RIGHT
            get() = X
    }
}

/**
 * Mutable vector for 3 float numbers
 */
interface Vector3fm : Vector3fc {
    override var x: Float
    override var y: Float
    override var z: Float
    fun negate() = negated(this)
    fun set(x: Float, y: Float, z: Float): Vector3fm {
        this.x = x
        this.y = y
        this.z = z
        return this
    }

    fun set(value: Float): Vector3fm = set(value, value, value)
    fun normalize() = normalize(this)
}

fun <T : Vector3fm> T.mul(other: Float): T {
    x *= other
    y *= other
    z *= other
    return this
}

fun Vector3fm.mul(matrix: Matrix4fc): Vector3fm {
    mul(matrix, this)
    return this
}

fun Vector3f.div(matrix: Matrix4fc): Vector3f {
    div(matrix, this)
    return this
}

fun Vector3fc.div(matrix: Matrix4fc, dest: Vector3fm): Vector3fm {
    val rx = matrix.m00 / x + matrix.m10 / y + matrix.m20 / z + matrix.m30
    val ry = matrix.m01 / x + matrix.m11 / y + matrix.m21 / z + matrix.m31
    val rz = matrix.m02 / x + matrix.m12 / y + matrix.m22 / z + matrix.m32
    dest.x = rx
    dest.y = ry
    dest.z = rz
    return dest
}

fun Vector3fc.mul(vector: Vector3fc, dest: Vector3fm): Vector3fm {
    dest.x = x * vector.x
    dest.y = x * vector.y
    dest.z = x * vector.z
    return dest
}

fun Vector3fc.mul(matrix: Matrix4fc, dest: Vector3fm): Vector3fm {
    val rx = matrix.m00 * x + matrix.m10 * y + matrix.m20 * z + matrix.m30
    val ry = matrix.m01 * x + matrix.m11 * y + matrix.m21 * z + matrix.m31
    val rz = matrix.m02 * x + matrix.m12 * y + matrix.m22 * z + matrix.m32
    dest.x = rx
    dest.y = ry
    dest.z = rz
    return dest
}

fun Vector3fm.sub(other: Vector3fc): Vector3fm = sub(other, this)

fun Vector3fc.sub(other: Vector3fc, dest: Vector3fm): Vector3fm {
    dest.set(
            x - other.x,
            y - other.y,
            z - other.z
    )
    return dest
}

inline fun Vector3fm.set(other: Vector3fc) = set(other.x, other.y, other.z)

inline fun <T : Vector3fm> T.add(other: Vector3fc) = add(other.x, other.y, other.z)

fun Vector3fc.add(other: Vector3fc, dest: Vector3fm): Vector3fm {
    dest.set(x + other.x, y + other.y, z + other.z)
    return dest
}

inline fun <T : Vector3fm> T.add(x: Float, y: Float, z: Float) = set(
        this.x + x,
        this.y + y,
        this.z + z
)

operator fun Vector3fm.timesAssign(vector: Vector3fc) {
    mul(vector, this)
}

operator fun Vector3fm.timesAssign(matrix: Matrix4fc) {
    mul(matrix, this)
}

open class Vector3f(override var x: Float = 0f, override var y: Float = 0f, override var z: Float = 0f) : Vector3fm {
    constructor(other: Vector3fc) : this(other.x, other.y, other.z)

    fun lerp(other: Vector3fc, t: Float) = lerp(other, t, this)


    //    inline fun set(other: Vector3fc) = set(other.x, other.y, other.z)
    override fun toString(): String = "Vec3f($x,$y,$z)"
}

operator fun Vector3fm.plusAssign(other: Vector3fc) {
    add(other.x, other.y, other.z)
//    set(
//            x + other.x,
//            y + other.y,
//            z + other.z
//    )
}

operator fun Vector3fc.minus(other: Vector3fc): Vector3f =
        Vector3f(x - other.x, y - other.y, z - other.z)

operator fun Vector3fc.unaryMinus() = negated()
operator fun Vector3fc.times(matrix: Matrix4fc) = this.mul(matrix, Vector3f())
operator fun Vector3fc.times(value: Float) = Vector3f(x * value, y * value, z * value)
operator fun Vector3fc.times(other: Vector3fc) = Vector3f(x * other.x, y * other.y, z * other.z)
operator fun Vector3fc.div(v: Vector3fc): Vector3f = Vector3f(x / v.x, y / v.y, z / v.z)
operator fun Vector3fc.div(other: Float): Vector3f = Vector3f(x / other, y / other, z / other)
interface Vector3ic {
    val x: Int
    val y: Int
    val z: Int
}

class Vector3i(override var x: Int = 0, override var y: Int = 0, override var z: Int = 0) : Vector3ic {

}

fun Vector3fc.cross(vector: Vector3fc, dest: Vector3fm): Vector3fm = cross(vector.x, vector.y, vector.z, dest)

fun Vector3fc.cross(x: Float, y: Float, z: Float, dest: Vector3fm): Vector3fm {
    val rx: Float = this.y * z - this.z * y
    val ry: Float = this.z * x - this.x * z
    val rz: Float = this.x * y - this.y * x
    dest.x = rx
    dest.y = ry
    dest.z = rz
    return dest
}

fun Vector3fc.dot(other: Vector3fc): Float = dot(other.x, other.y, other.z)

fun Vector3fc.dot(x: Float, y: Float, z: Float): Float {
    return this.x * x + this.y * y + this.z * z
}

val VECTOR_UP: Vector3fc = Vector3f(0f, 1f, 0f)
val VECTOR_LEFT: Vector3fc = Vector3f(-1f, 1f, 0f)

fun orthogonalize(v: Vector3fc, dest: Vector3fm): Vector3fm {
    val rx: Float
    val ry: Float
    val rz: Float
    if (abs(v.x) > abs(v.z)) {
        rx = -v.y
        ry = v.x
        rz = 0.0f
    } else {
        rx = 0.0f
        ry = -v.z
        rz = v.y
    }
    val invLen = 1.0f / sqrt(rx * rx + ry * ry + (rz * rz))
    dest.x = rx * invLen
    dest.y = ry * invLen
    dest.z = rz * invLen
    return dest
}

val Vector3fc.isNaN
    get() = x.isNaN() || y.isNaN() || z.isNaN()

class Vector3fProperty(x: Float = 0f, y: Float = 0f, z: Float = 0f) : Vector3f(x, y, z) {
    private var changeFlag = true
    override var x: Float
        get() = super.x
        set(value) {
            if (!changeFlag && value != super.x)
                changeFlag = true
            super.x = value
        }

    override var y: Float
        get() = super.y
        set(value) {
            if (!changeFlag && value != super.y)
                changeFlag = true
            super.y = value
        }

    override var z: Float
        get() = super.z
        set(value) {
            if (!changeFlag && value != super.z)
                changeFlag = true
            super.z = value
        }

    fun resetChangeFlag(): Boolean {
        val b = changeFlag
        changeFlag = false
        return b
    }
}