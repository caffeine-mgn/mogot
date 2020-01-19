package mogot.collider

import mogot.Spatial
import mogot.Spatial2D
import mogot.math.Vector2fc

interface Collider2D {
    var node: Spatial2D?

    fun test(x: Float, y: Float): Boolean
    fun test(point: Vector2fc) = test(point.x, point.y)
}