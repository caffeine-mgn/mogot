package mogot.collider

import mogot.Spatial2D
import mogot.math.Vector2f

class Circle2DCollider : Collider2D {
    override var node: Spatial2D? = null
    var radius = 0f

    override fun test(x: Float, y: Float): Boolean {
        val node = node ?: return false
        val pos = Vector2f(x, y).let { node.globalToLocal(it, it) }
        return pos.lengthSquared <= radius * radius
    }

}
