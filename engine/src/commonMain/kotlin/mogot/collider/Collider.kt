package mogot.collider

import mogot.Spatial
import mogot.math.*

interface Collider {
    var node: Spatial?
    fun rayCast(ray: Ray, dest: Vector3fm?): Boolean// = rayCast(ray.position, ray.direction, dest)

    companion object {
        fun rayCastTrangle(p0: Vector3fc, p1: Vector3fc, p2: Vector3fc, rayPosition: Vector3fc, rayDirection: Vector3fc, dest: Vector3fm?): Boolean {
            val f = Intersectionf.intersectRayTriangle(
                    rayPosition, rayDirection,
                    p0,
                    p1,
                    p2,
                    0f
            )
            if (f != -1f && dest != null) {
                dest.set(rayDirection).mul(f).add(rayPosition)
            }
            return f != -1f
        }
    }
}