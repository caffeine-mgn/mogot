package mogot.collider

import mogot.Spatial
import mogot.math.*

interface Collider {
    var node: Spatial?
    fun rayCast(ray: Ray, dest: Vector3fm?): Boolean// = rayCast(ray.position, ray.direction, dest)

    companion object {

        fun rayCastTrangle(
                originX: Float, originY: Float, originZ: Float,
                dirX: Float, dirY: Float, dirZ: Float,
                p0X: Float, p0Y: Float, p0Z: Float,
                p1X: Float, p1Y: Float, p1Z: Float,
                p2X: Float, p2Y: Float, p2Z: Float,
                dest: Vector3fm?): Boolean {
            val f = Intersectionf.intersectRayTriangle(
                    originX, originY, originZ,
                    dirX, dirY, dirZ,
                    p0X, p0Y, p0Z,
                    p1X, p1Y, p1Z,
                    p2X, p2Y, p2Z,
                    0f
            )
            if (f != -1f && dest != null) {
                dest.set(dirX, dirY, dirZ).mul(f).add(originX, originY, originZ)
            }
            return f != -1f
        }

        fun rayCastTrangle(p0: Vector3fc, p1: Vector3fc, p2: Vector3fc, rayPosition: Vector3fc, rayDirection: Vector3fc, dest: Vector3fm?): Boolean {
            return rayCastTrangle(
                    rayPosition.x, rayPosition.y, rayPosition.z,
                    rayDirection.x, rayDirection.y, rayDirection.z,
                    p0.x, p0.y, p0.z,
                    p1.x, p1.y, p1.z,
                    p2.x, p2.y, p2.z,
                    dest
            )
        }
    }
}