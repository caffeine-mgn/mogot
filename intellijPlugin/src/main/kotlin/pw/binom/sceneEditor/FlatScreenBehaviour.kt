package pw.binom.sceneEditor

import mogot.*
import mogot.math.Vector2i

class FlatScreenBehaviour(val engine: Engine, val camera: Camera, val other: Spatial) : Behaviour() {
    public override val node
        get() = super.node as SpriteFor3D

    override fun checkNode(node: Node?) {
        node as SpriteFor3D?
    }

    override fun onUpdate(delta: Float) {
        val p = engine.mathPool.vec3f.poll()
        p.set(0f, 0f, 0f)
        other.localToGlobal(p, p)

        val tempVec = engine.mathPool.vec2i.poll()
        if (camera.worldToScreenPoint(p, tempVec)) {
            node.visible = true
            node.position.set(tempVec.x.toFloat(), tempVec.y.toFloat())
        } else {
            node.visible = false
        }
        engine.mathPool.vec3f.push(p)
        engine.mathPool.vec2i.push(tempVec)
    }
}