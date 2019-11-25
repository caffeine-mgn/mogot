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