@file:JvmName("Vec3KKT")

package mogot.math

import kotlin.jvm.JvmName
import kotlin.math.sqrt

/*
expect interface Vector3fc {
    @JsName("getX")
    fun x(): Float

    @JsName("getY")
    fun y(): Float

    @JsName("getZ")
    fun z(): Float
}

object Vec3f {
    val UP = Vector3f(0f, 1f, 0f)
}

//val Vector3fc.Companion.

expect class Vector3f : Vector3fc {
    constructor(x: Float, y: Float, z: Float)
    constructor()

    @JvmField
    var x: Float

    @JvmField
    var y: Float

    @JvmField
    var z: Float

    fun add(other: Vector3fc): Vector3f
    fun set(other: Vector3fc): Vector3f
    fun set(x: Float, y: Float, z: Float): Vector3f
    fun negate(): Vector3f
}
*/

interface Vector3fc {
    val x: Float

    val y: Float

    val z: Float

    fun lerp(other: Vector3fc, t: Float, dest: Vector3f): Vector3f {
        dest.x = x + (other.x - x) * t
        dest.y = y + (other.y - y) * t
        dest.z = z + (other.z - z) * t
        return dest
    }

    fun normalize(dest: Vector3f): Vector3f {
        val invLength: Float = 1.0f / length
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


    companion object {
        val UP: Vector3fc = Vector3f(0f, 1f, 0f)
    }
}

open class Vector3f(override var x: Float = 0f, override var y: Float = 0f, override var z: Float = 0f) : Vector3fc {
    fun set(x: Float, y: Float, z: Float): Vector3f {
        this.x = x
        this.y = y
        this.z = z
        return this
    }

    fun lerp(other: Vector3fc, t: Float) = lerp(other, t, this)

    fun normalize() = normalize(this)

    inline fun set(other: Vector3fc) = set(other.x, other.y, other.z)
    fun add(x: Float, y: Float, z: Float): Vector3f {
        this.x += x
        this.y += y
        this.z += z
        return this
    }

    inline fun add(other: Vector3fc) = add(other.x, other.y, other.z)
    fun negate() = Vector3f(-x, -y, -z)

    override fun toString(): String = "Vec3f($x,$y,$z)"
    operator fun plusAssign(other: Vector3fc) {
        set(
                x + other.x,
                y + other.y,
                z + other.z
        )
    }
}

operator fun Vector3fc.minus(other: Vector3f): Vector3f =
        Vector3f(x - other.x, y - other.y, z - other.z)

operator fun Vector3fc.unaryMinus() = Vector3f(-x, -y, -z)

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