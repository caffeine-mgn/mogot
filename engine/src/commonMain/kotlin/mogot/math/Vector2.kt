package mogot.math

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

interface Vector2ic {
    val x: Int
    val y: Int
}

interface Vector2im : Vector2ic {
    override var x: Int
    override var y: Int
    fun set(x: Int, y: Int): Vector2im {
        this.x = x
        this.y = y
        return this
    }
}

class Vector2i(override var x: Int = 0, override var y: Int = 0) : Vector2im {
    constructor(other: Vector2ic) : this(other.x, other.y)
}

interface Vector2fc {
    val x: Float
    val y: Float

    val lengthSquared
        get() = x * x + y * y

    companion object {
        fun rotate(sourceX: Float, sourceY: Float, angle: Float, dest: Vector2fm): Vector2fm {
            val cs = cos(angle)
            val sn = sin(angle)
            val px = sourceX * cs - sourceY * sn
            val py = sourceX * sn + sourceY * cs
            dest.set(px, py)
            return dest
        }
    }
}

interface Vector2fm : Vector2fc {
    override var x: Float
    override var y: Float
    fun set(x: Float, y: Float): Vector2fm {
        this.x = x
        this.y = y
        return this
    }
}

fun Vector2fc.add(x: Float, y: Float, dest: Vector2fm): Vector2fm {
    dest.x = this.x + x
    dest.y = this.y + y
    return dest
}

fun Vector2fm.add(x: Float, y: Float) = add(x, y, this)
fun Vector2fm.add(other: Vector2fc) = add(other.x, other.y, this)

fun Vector2ic.add(x: Int, y: Int, dest: Vector2im): Vector2im {
    dest.x = this.x + x
    dest.y = this.y + y
    return dest
}

fun Vector2im.add(x: Int, y: Int) = add(x, y, this)

open class Vector2f(override var x: Float = 0f, override var y: Float = 0f) : Vector2fm {
    constructor(other: Vector2fc) : this(other.x, other.y)

    override fun toString(): String = "Vec2f($x $y)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Vector2f

        if (x != other.x) return false
        if (y != other.y) return false

        return true
    }

    override fun hashCode(): Int {
        var result = x.hashCode()
        result = 31 * result + y.hashCode()
        return result
    }


}

inline fun Vector2fm.set(other: Vector2fc) = set(other.x, other.y)
inline fun Vector2im.set(other: Vector2ic) = set(other.x, other.y)
fun Vector2fm.normalize(): Vector2fm = normalize(this)
fun Vector2fc.normalized(): Vector2fm = normalize(Vector2f())

fun Vector2fc.normalize(dest: Vector2fm): Vector2fm {
    val lengthSquared = lengthSquared
    if (lengthSquared == 1f || lengthSquared == 0f)
        return dest
    val invLength: Float = 1.0f / sqrt(lengthSquared)
    dest.x = x * invLength
    dest.y = y * invLength
    return dest
}

inline fun Vector2fc.rotate(angle: Float, dest: Vector2fm) = Vector2fc.rotate(x, y, angle, dest)

inline val Vector2fc.angle
    get() = atan2(y, x)

inline fun Vector2fm.rotate(angle: Float) = rotate(angle, this)

fun Vector2fc.mulXY(matrix: Matrix4fc, dest: Vector2fm): Vector2fm {
    val rx = matrix.m00 * x + matrix.m10 * y + matrix.m30
    val ry = matrix.m01 * x + matrix.m11 * y + matrix.m31
    dest.x = rx
    dest.y = ry
    return dest
}

open class Vector2fProperty(x: Float = 0f, y: Float = 0f) : Vector2f(x, y) {
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

    fun resetChangeFlag(): Boolean {
        val b = changeFlag
        changeFlag = false
        return b
    }
}