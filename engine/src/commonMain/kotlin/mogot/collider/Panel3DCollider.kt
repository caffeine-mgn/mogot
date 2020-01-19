package mogot.collider

import mogot.Spatial
import mogot.math.*

class Panel3DCollider(width: Float, height: Float) : Collider {
    private val TEMP_MATRIX = Matrix4f()
    override var node: Spatial? = null

    private val p1 = Vector3f()
    private val p2 = Vector3f()
    private val p3 = Vector3f()
    private val p4 = Vector3f()

    var width: Float = width
        set(value) {
            field = value
            update()
        }
    var height: Float = height
        set(value) {
            field = value
            update()
        }

    private fun update() {
        p1.set(-width / 2, 0f, -height / 2f)
        p2.set(width / 2, 0f, -height / 2f)
        p3.set(-width / 2, 0f, height / 2f)
        p4.set(width / 2, 0f, height / 2f)
    }

    private val tempRay = MutableRay()
    override fun rayCast(ray: Ray, dest: Vector3fm?): Boolean {
        val node = node ?: return false
        node.globalToLocalMatrix(TEMP_MATRIX)
        ray.mul(TEMP_MATRIX, tempRay)
        val localPosition = tempRay.position
        val localDirection = tempRay.direction

        update()
        node.localToGlobalMatrix(TEMP_MATRIX)
        if (Collider.rayCastTrangle(p1, p2, p4, localPosition, localDirection, dest)) {
            dest?.mul(TEMP_MATRIX)
            return true
        }

        if (Collider.rayCastTrangle(p1, p3, p4, localPosition, localDirection, dest)) {
            dest?.mul(TEMP_MATRIX)
            return true
        }
        return false
    }

}