package game.game

import mogot.Behaviour
import mogot.Camera
import mogot.Engine
import mogot.annotations.Property
import mogot.annotations.RestrictionMax
import mogot.annotations.RestrictionMin
import mogot.math.*

const val KEY_W = 87
const val KEY_S = 83
const val KEY_D = 68
const val KEY_A = 65
const val KEY_E = 69
const val KEY_Q = 81

class FpsCamB(val engine: Engine) : Behaviour() {

    override val node: Camera
        get() = super.node as Camera

    //    @Property
//    fun getValue(){
//
//    }
    var normalSpeed = 3f

    var startPosition: Vector3f? = null

    var fastSpeed = 6f

    var color = Vector3f()

    private var x = 0f
    private var y = 0f

    val yQ = Quaternionf()
    val pQ = Quaternionf()
    private val temp = Vector3f()

    override fun onStart() {
        super.onStart()
        y = -node.quaternion.roll
        x = -node.quaternion.pitch
        println("Start! $x $y")
    }

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

        if (engine.stage.isKeyDown(KEY_W)) {
            node.position.add(temp)
//            node.position.add(node.rotation.forward * dt * moveSpeed)
        }

        if (engine.stage.isKeyDown(KEY_S)) {
            temp.negated(temp)
            node.position.add(temp)
//            node.position.add(-node.rotation.forward * dt * moveSpeed)
        }

        temp.set(delta * moveSpeed, 0f, 0f)
        node.quaternion.mul(temp, temp)
        if (engine.stage.isKeyDown(KEY_D)) {
            node.position.add(temp)
        }
        if (engine.stage.isKeyDown(KEY_A)) {
            temp.negated(temp)
            node.position.add(temp)
        }

        temp.set(0f, moveSpeed * delta, 0f)
        node.quaternion.mul(temp, temp)
        if (engine.stage.isKeyDown(KEY_E)) {
            node.position.add(temp)
        }
        if (engine.stage.isKeyDown(KEY_Q)) {
            temp.negated(temp)
            node.position.add(temp)
        }
        if (engine.stage.isMouseDown(2) || engine.stage.isMouseDown(3)) {
            engine.stage.cursorVisible = false

            val centerX = engine.stage.size.x / 2
            val centerY = engine.stage.size.y / 2
            x += (engine.stage.mousePosition.x - centerX) * 0.005f
            y += (engine.stage.mousePosition.y - centerY) * 0.005f
            node.quaternion.identity()
            node.quaternion.setRotation(0f, -x, -y)
        } else {
            engine.stage.cursorVisible = true
        }
    }
}