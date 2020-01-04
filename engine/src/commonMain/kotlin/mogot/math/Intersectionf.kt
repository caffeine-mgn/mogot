package mogot.math

object Intersectionf {
    fun intersectRayTriangle(origin: Vector3fc, dir: Vector3fc, v0: Vector3fc, v1: Vector3fc, v2: Vector3fc, epsilon: Float) =
            intersectRayTriangle(
                    origin.x, origin.y, origin.z,
                    dir.x, dir.y, dir.z,
                    v0.x, v0.y, v0.z,
                    v1.x, v1.y, v1.z,
                    v2.x, v2.y, v2.z,
                    epsilon
            )

    fun intersectRayTriangle(originX: Float, originY: Float, originZ: Float, dirX: Float, dirY: Float, dirZ: Float,
                             v0X: Float, v0Y: Float, v0Z: Float, v1X: Float, v1Y: Float, v1Z: Float, v2X: Float, v2Y: Float, v2Z: Float,
                             epsilon: Float): Float {
        val edge1X = v1X - v0X
        val edge1Y = v1Y - v0Y
        val edge1Z = v1Z - v0Z
        val edge2X = v2X - v0X
        val edge2Y = v2Y - v0Y
        val edge2Z = v2Z - v0Z
        val pvecX = dirY * edge2Z - dirZ * edge2Y
        val pvecY = dirZ * edge2X - dirX * edge2Z
        val pvecZ = dirX * edge2Y - dirY * edge2X
        val det = edge1X * pvecX + edge1Y * pvecY + edge1Z * pvecZ
        if (det > -epsilon && det < epsilon) return -1.0f
        val tvecX = originX - v0X
        val tvecY = originY - v0Y
        val tvecZ = originZ - v0Z
        val invDet = 1.0f / det
        val u = (tvecX * pvecX + tvecY * pvecY + tvecZ * pvecZ) * invDet
        if (u < 0.0f || u > 1.0f) return -1.0f
        val qvecX = tvecY * edge1Z - tvecZ * edge1Y
        val qvecY = tvecZ * edge1X - tvecX * edge1Z
        val qvecZ = tvecX * edge1Y - tvecY * edge1X
        val v = (dirX * qvecX + dirY * qvecY + dirZ * qvecZ) * invDet
        return if (v < 0.0f || u + v > 1.0f) -1.0f else (edge2X * qvecX + edge2Y * qvecY + edge2Z * qvecZ) * invDet
    }
}