package mogot.math

interface AABBc {
    val position: Vector3fc
    val sizes: Vector3fc
}

interface AABBm : AABBc {
    override val position: Vector3fm
    override val sizes: Vector3fm
}

class AABB : AABBm {
    override val position = Vector3f()
    override val sizes = Vector3f()
}