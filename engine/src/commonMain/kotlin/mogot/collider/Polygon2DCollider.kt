package mogot.collider

import mogot.Spatial2D
import mogot.math.Vector2f
import mogot.math.Vector2fc

class Polygon2DCollider(override var node: Spatial2D?, var vertex: List<Vector2fc>? = null) : Collider2D {

    // Given three colinear points p, q, r,
// the function checks if point q lies
// on line segment 'pr'
    private fun onSegment(p: Vector2fc, q: Vector2fc, r: Vector2fc): Boolean {
        return q.x <= maxOf(p.x, r.x) && q.x >= minOf(p.x, r.x) && q.y <= maxOf(p.y, r.y) && q.y >= minOf(p.y, r.y)
    }

    private fun orientation(p: Vector2fc, q: Vector2fc, r: Vector2fc): Int {
        val value = ((q.y - p.y) * (r.x - q.x)
                - (q.x - p.x) * (r.y - q.y))
        if (value == 0f) {
            return 0 // colinear
        }
        return if (value > 0) 1 else 2 // clock or counterclock wise
    }

    fun doIntersect(p1: Vector2fc, q1: Vector2fc,
                    p2: Vector2fc, q2: Vector2fc): Boolean { // Find the four orientations needed for
// general and special cases
        val o1 = orientation(p1, q1, p2)
        val o2 = orientation(p1, q1, q2)
        val o3 = orientation(p2, q2, p1)
        val o4 = orientation(p2, q2, q1)
        // General case
        if (o1 != o2 && o3 != o4) {
            return true
        }
        // Special Cases
// p1, q1 and p2 are colinear and
// p2 lies on segment p1q1
        if (o1 == 0 && onSegment(p1, p2, q1)) {
            return true
        }
        // p1, q1 and p2 are colinear and
// q2 lies on segment p1q1
        if (o2 == 0 && onSegment(p1, q2, q1)) {
            return true
        }
        // p2, q2 and p1 are colinear and
// p1 lies on segment p2q2
        if (o3 == 0 && onSegment(p2, p1, q2)) {
            return true
        }
        // p2, q2 and q1 are colinear and
// q1 lies on segment p2q2
        return if (o4 == 0 && onSegment(p2, q1, q2)) {
            true
        } else false
        // Doesn't fall in any of the above cases
    }

    // Returns true if the point p lies
// inside the polygon[] with n vertices
    fun isInside(polygon: List<Vector2fc>, p: Vector2fc): Boolean { // There must be at least 3 vertices in polygon[]
        val n = polygon.size
        if (n < 3) {
            return false
        }
        // Create a point for line segment from p to infinite
        val extreme = Vector2f(Float.MAX_VALUE, p.y)
        // Count intersections of the above line
// with sides of polygon
        var count = 0
        var i = 0
        do {
            val next = (i + 1) % n
            // Check if the line segment from 'p' to
// 'extreme' intersects with the line
// segment from 'polygon[i]' to 'polygon[next]'
            if (doIntersect(polygon[i], polygon[next], p, extreme)) { // If the point 'p' is colinear with line
// segment 'i-next', then check if it lies
// on segment. If it lies, return true, otherwise false
                if (orientation(polygon[i], p, polygon[next]) == 0) {
                    return onSegment(polygon[i], p,
                            polygon[next])
                }
                count++
            }
            i = next
        } while (i != 0)
        // Return true if count is odd, false otherwise
        return count % 2 == 1 // Same as (count%2 == 1)
    }

    override fun test(x: Float, y: Float): Boolean {
        val node = node ?: return false
        val vertex = vertex ?: return false
        val tmp = node.engine.mathPool.vec2f.poll()
        tmp.set(x, y)
        node.globalToLocal(tmp, tmp)
        val result = isInside(vertex, tmp)
        node.engine.mathPool.vec2f.push(tmp)

        return result
    }
}