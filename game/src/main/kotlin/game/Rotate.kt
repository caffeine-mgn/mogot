package game

import mogot.Node
import mogot.Spatial
import org.tlsys.engine.Behaviour

class Rotate:Behaviour(){
    override val node
        get() = super.node as Spatial

    override fun onUpdate(delta: Float) {
        super.onUpdate(delta)
        node.quaternion.rotateX(delta)
    }
}