package pw.binom.sceneEditor

import mogot.*
import mogot.math.Vector2i

class FlatScreenBehaviour(val camera: Camera, val other: Spatial) : Behaviour() {
    public override val node
        get() = super.node as Sprite

    override fun checkNode(node: Node?) {
        node as Sprite?
    }

    override fun onUpdate(delta: Float) {
        val p2 = tempVec
        if (camera.worldToScreenPoint(other.matrix, p2)) {
            node.visible = true
            node.position.set(p2.x.toFloat() - node.size.x / 2f, p2.y.toFloat() - node.size.y / 2f)
        } else {
            node.visible = false
        }
    }

    private val tempVec = Vector2i()
}