package mogot.math

interface Vector2ic {
    val x: Int
    val y: Int
}

class Vector2i(override var x: Int = 0, override var y: Int = 0) : Vector2ic {
    fun set(x: Int, y: Int): Vector2i {
        this.x = x
        this.y = y
        return this
    }

    inline fun set(other: Vector2ic) = set(other.x, other.y)
}

interface Vector2fc {
    val x: Float
    val y: Float
}

class Vector2f(override var x: Float = 0f, override var y: Float = 0f) : Vector2fc {
    constructor(other: Vector2fc) : this(other.x, other.y)

    fun set(x: Float, y: Float): Vector2f {
        this.x = x
        this.y = y
        return this
    }

    inline fun set(other: Vector2fc) = set(other.x, other.y)
}