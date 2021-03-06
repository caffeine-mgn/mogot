package mogot.math

interface Vector4fc {
    val x: Float
    val y: Float
    val z: Float
    val w: Float
}


fun Vector4fm.mul(mat: Matrix4fc): Vector4fm = mul(mat, this)

fun Vector4fc.mul(mat: Matrix4fc, dest: Vector4fm): Vector4fm =
        if (mat.properties and PROPERTY_AFFINE != 0)
            mulAffine(mat, dest)
        else
            mulGeneric(mat, dest)

private fun Vector4fc.mulAffine(mat: Matrix4fc, dest: Vector4fm): Vector4fm {
    val rx: Float = mat.m00 * x + mat.m10 * y + mat.m20 * z + mat.m30 * w
    val ry: Float = mat.m01 * x + mat.m11 * y + mat.m21 * z + mat.m31 * w
    val rz: Float = mat.m02 * x + mat.m12 * y + mat.m22 * z + mat.m32 * w
    dest.x = rx
    dest.y = ry
    dest.z = rz
    dest.w = w
    return dest
}

private fun Vector4fc.mulGeneric(mat: Matrix4fc, dest: Vector4fm): Vector4fm {
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

interface Vector4fm : Vector4fc {
    override var x: Float
    override var y: Float
    override var z: Float
    override var w: Float

    fun set(x: Float, y: Float, z: Float, w: Float): Vector4fm {
        this.x = x
        this.y = y
        this.z = z
        this.w = w
        return this
    }
}

inline fun Vector4fm.set(other: Vector4fc) = set(other.x, other.y, other.z, other.w)

open class Vector4f(override var x: Float = 0f, override var y: Float = 0f, override var z: Float = 0f, override var w: Float = 0f) : Vector4fm {

    companion object {
        fun fromColor(a: Int, r: Int, g: Int, b: Int): Vector4f {
            check(r in 0..255)
            check(g in 0..255)
            check(b in 0..255)
            check(a in 0..255)
            fun Int.div() = if (this == 0) 0f else this / 255f
            return Vector4f(r.div(), g.div(), b.div(), a.div())
        }

        fun fromColor(color: Int): Vector4f {
            val r = color and 0xFF
            val g = color shr 8 and 0xFF
            val b = color shr 16 and 0xFF
            val a = color shr 24 and 0xFF
            return fromColor(a, r, g, b)
        }
    }

    constructor(other: Vector4fc) : this(other.x, other.y, other.z, other.w)
    constructor(value: Float) : this(value, value, value, value)

    override fun toString(): String =
            "Vec4f($x $y $y $w)"
}

val Vector4fc.isNaN
    get() = x.isNaN() || y.isNaN() || z.isNaN() || w.isNaN()

class Vector4fProperty(x: Float = 0f, y: Float = 0f, z: Float = 0f, w: Float = 0f) : Vector4f(x, y, z, w) {
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

    override var w: Float
        get() = super.z
        set(value) {
            if (!changeFlag && value != super.z)
                changeFlag = true
            super.z = value
        }

    fun resetChangeFlag(): Boolean {
        val b = changeFlag
        changeFlag = true
        return b
    }
}