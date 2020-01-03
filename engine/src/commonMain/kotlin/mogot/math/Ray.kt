package mogot.math

interface Ray{
    val position: Vector3fc
    val direction: Vector3fc
}

class MutableRay(
        override val position: Vector3f = Vector3f(),
        override val direction: Vector3f = Vector3f(VECTOR_UP)
) : Ray {
    override fun toString(): String = "MutableRay(position=$position, direction=$direction)"
}