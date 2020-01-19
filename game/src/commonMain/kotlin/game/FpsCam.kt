package game

import mogot.Behaviour
import mogot.Camera
import mogot.Engine
import mogot.math.*

class FpsCam(val engine: Engine) : Behaviour() {
    override val node: Camera
        get() = super.node as Camera

    var normalSpeed = 3f

    var fastSpeed = 6f

    private var x = 0f
    private var y = 0f
    private val temp = Vector3f()

    override fun onUpdate(delta: Float) {


        engine.stage.mouseDown.on {
            if (it == 2 || it == 3)
                engine.stage.lockMouse = true
        }
        engine.stage.mouseUp.on {
            if (it == 2 || it == 3)
                engine.stage.lockMouse = false
        }
        val delta = 0.01f
        super.onUpdate(delta)

        val moveSpeed = if (engine.stage.isKeyDown(340)) fastSpeed else normalSpeed

        temp.set(0f, 0f, -(delta * moveSpeed))
        node.quaternion.mul(temp, temp)
        if (engine.stage.isKeyDown(87)) {
            node.position.add(temp)
//            node.position.add(node.rotation.forward * dt * moveSpeed)
        }

        if (engine.stage.isKeyDown(83)) {
            temp.negated(temp)
            node.position.add(temp)
//            node.position.add(-node.rotation.forward * dt * moveSpeed)
        }

        temp.set(delta * moveSpeed, 0f, 0f)
        node.quaternion.mul(temp, temp)
        if (engine.stage.isKeyDown(68)) {
            node.position.add(temp)
        }
        if (engine.stage.isKeyDown(65)) {
            temp.negated(temp)
            node.position.add(temp)
        }

        temp.set(0f, moveSpeed * delta, 0f)
        node.quaternion.mul(temp, temp)
        if (engine.stage.isKeyDown(69)) {
            node.position.add(temp)
        }
        if (engine.stage.isKeyDown(81)) {
            temp.negated(temp)
            node.position.add(temp)
        }
        if (engine.stage.isMouseDown(2) || engine.stage.isMouseDown(3)) {
            engine.stage.cursorVisible = false

            val centerX = engine.stage.size.x / 2
            val centerY = engine.stage.size.y / 2
            x += (engine.stage.mousePosition.x - centerX) * delta / 5f
            y += (engine.stage.mousePosition.y - centerY) * delta / 5f
            node.quaternion.identity()
            node.quaternion.setRotation(0f, -x, -y)
        } else {
            engine.stage.cursorVisible = true
        }
    }
}