package mogot.math

interface AABBc {
    val position: Vector3fc
    val size: Vector3fc
}

interface AABBm : AABBc {
    override val position: Vector3fm
    override val size: Vector3fm
}

class AABB : AABBm {
    override val position = Vector3f()
    override val size = Vector3f()
}