package mogot.collider

import mogot.Spatial2D
import mogot.math.Intersectionf
import mogot.math.Vector2f

class Panel2DCollider : Collider2D {
    override var node: Spatial2D? = null
    val size = Vector2f()

    override fun test(x: Float, y: Float): Boolean {
        val node = node ?: return false
        val pos = Vector2f(x, y).let { node.globalToLocal(it, it) }

        return Intersectionf.testPointAar(
                pos.x, pos.y,
                -size.x * 0.5f, -size.y * 0.5f,
                size.x * 0.5f, size.y * 0.5f
        )
    }

}
