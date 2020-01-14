package mogot.collider

import mogot.Spatial
import mogot.math.*

class BoxCollider : Collider {
    private val TEMP_MATRIX = Matrix4f()
    private val tempRay = MutableRay()
    private val tempVec2f = Vector2f()

    override var node: Spatial? = null
    val size = Vector3f()

    override fun rayCast(ray: Ray, dest: Vector3fm?): Boolean =
            rayCast(ray, dest, null)

    fun rayCast(ray: Ray, near: Vector3fm?, far: Vector3fm?): Boolean {
        val node = node ?: return false
        node.globalToLocalMatrix(TEMP_MATRIX)
        ray.mul(TEMP_MATRIX, tempRay)
        val localPosition = tempRay.position
        val localDirection = tempRay.direction

        if (!Intersectionf.intersectRayAab(
                        localPosition.x, localPosition.y, localPosition.z,
                        localDirection.x, localDirection.y, localDirection.z,
                        -size.x / 2, -size.y / 2, -size.z / 2,
                        size.x / 2, size.y / 2, size.z / 2,
                        tempVec2f
                )) return false

        near?.set(localDirection)?.mul(tempVec2f.x)?.add(localPosition)?.mul(TEMP_MATRIX)
        far?.set(localDirection)?.mul(tempVec2f.y)?.add(localPosition)?.mul(TEMP_MATRIX)
        return true
    }

}