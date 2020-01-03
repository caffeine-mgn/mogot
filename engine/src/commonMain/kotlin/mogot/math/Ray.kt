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

fun Ray.mul(matrix: Matrix4f, dest: MutableRay): MutableRay {
    position.mul(matrix, dest.position)
    direction.add(position, dest.direction).mul(matrix, dest.direction).sub(dest.position).normalize()
    return dest
}