package pw.binom.sceneEditor

import mogot.*
import mogot.math.Vector2i

class FlatScreenBehaviour2D(val other: Spatial2D) : Behaviour() {
    public override val node
        get() = super.node as Sprite

    override fun checkNode(node: Node?) {
        node as Sprite?
    }

    override fun onUpdate(delta: Float) {
        node.position.set(0f,0f)
        other.localToGlobal(node.position, node.position)
    }
}