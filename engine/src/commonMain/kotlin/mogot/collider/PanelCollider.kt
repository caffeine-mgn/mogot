package mogot.collider

import mogot.Spatial
import mogot.math.*

class PanelCollider(width: Float, height: Float) : Collider {
    private val TEMP_MATRIX = Matrix4f()
    override var node: Spatial? = null
    private val LOCAL_POS = Vector3f()
    private val LOCAL_Dir = Vector3f()

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

//        p1 *= node.matrix
//        p2 *= node.matrix
//        p3 *= node.matrix
//        p4 *= node.matrix
    }

    override fun rayCast(position: Vector3fc, direction: Vector3fc, dest: Vector3fm?): Boolean {
        node!!.globalTransfrorm(TEMP_MATRIX)
        val localPosition = position.mul(TEMP_MATRIX, LOCAL_POS)
        val localDirection = direction.mul(TEMP_MATRIX, LOCAL_Dir)

        update()

        if (Collider.rayCastTrangle(p1, p2, p4, localPosition, localDirection, dest)) {
            dest?.mul(TEMP_MATRIX)
            return true
        }

        if (Collider.rayCastTrangle(p1, p3, p4, localPosition, localDirection, dest)) {
            dest?.mul(TEMP_MATRIX)
            return true
        }
        return false

        var f = Intersectionf.intersectRayTriangle(
                localPosition, localDirection,
                p1,
                p2,
                p4,
                0f
        )
        if (f == -1f) {
            f = Intersectionf.intersectRayTriangle(
                    localPosition, localDirection,
                    p1,
                    p3,
                    p4,
                    0f
            )
        }

        if (f != -1f && dest != null) {
            dest.set(localDirection).mul(f).add(localPosition).mul(node!!.matrix)
        }

        return f != -1f

    }

}