package mogot.math

object Intersectionf {

    /**
     * Test whether the given point `(pX, pY)` lies inside the axis-aligned rectangle with the minimum corner `(minX, minY)`
     * and maximum corner `(maxX, maxY)`.
     *
     * @param pX the x coordinate of the point
     * @param pY the y coordinate of the point
     * @param minX the x coordinate of the minimum corner of the axis-aligned rectangle
     * @param minY the y coordinate of the minimum corner of the axis-aligned rectangle
     * @param maxX the x coordinate of the maximum corner of the axis-aligned rectangle
     * @param maxY the y coordinate of the maximum corner of the axis-aligned rectangle
     * @return `true` iff the point lies inside the axis-aligned rectangle; `false` otherwise
     */
    fun testPointAar(pX: Float, pY: Float, minX: Float, minY: Float, maxX: Float, maxY: Float): Boolean {
        return pX >= minX && pY >= minY && pX <= maxX && pY <= maxY
    }

    fun intersectRayAab(originX: Float, originY: Float, originZ: Float, dirX: Float, dirY: Float, dirZ: Float,
                        minX: Float, minY: Float, minZ: Float, maxX: Float, maxY: Float, maxZ: Float, result: Vector2fm): Boolean {
        val invDirX = 1.0f / dirX
        val invDirY = 1.0f / dirY
        val invDirZ = 1.0f / dirZ
        var tNear: Float
        var tFar: Float
        val tymin: Float
        val tymax: Float
        val tzmin: Float
        val tzmax: Float
        if (invDirX >= 0.0f) {
            tNear = (minX - originX) * invDirX
            tFar = (maxX - originX) * invDirX
        } else {
            tNear = (maxX - originX) * invDirX
            tFar = (minX - originX) * invDirX
        }
        if (invDirY >= 0.0f) {
            tymin = (minY - originY) * invDirY
            tymax = (maxY - originY) * invDirY
        } else {
            tymin = (maxY - originY) * invDirY
            tymax = (minY - originY) * invDirY
        }
        if (tNear > tymax || tymin > tFar) return false
        if (invDirZ >= 0.0f) {
            tzmin = (minZ - originZ) * invDirZ
            tzmax = (maxZ - originZ) * invDirZ
        } else {
            tzmin = (maxZ - originZ) * invDirZ
            tzmax = (minZ - originZ) * invDirZ
        }
        if (tNear > tzmax || tzmin > tFar) return false
        tNear = if (tymin > tNear || tNear.isNaN()) tymin else tNear
        tFar = if (tymax < tFar || tFar.isNaN()) tymax else tFar
        tNear = if (tzmin > tNear) tzmin else tNear
        tFar = if (tzmax < tFar) tzmax else tFar
        if (tNear < tFar && tFar >= 0.0f) {
            result.x = tNear
            result.y = tFar
            return true
        }
        return false
    }

    fun intersectRayPlane(
            originX: Float, originY: Float, originZ: Float,
            dirX: Float, dirY: Float, dirZ: Float,
            pointX: Float, pointY: Float, pointZ: Float,
            normalX: Float, normalY: Float, normalZ: Float,
            epsilon: Float): Float {
        val denom = normalX * dirX + normalY * dirY + normalZ * dirZ
        if (denom < epsilon) {
            val t = ((pointX - originX) * normalX + (pointY - originY) * normalY + (pointZ - originZ) * normalZ) / denom
            if (t >= 0.0f)
                return t
        }
        return -1.0f
    }

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