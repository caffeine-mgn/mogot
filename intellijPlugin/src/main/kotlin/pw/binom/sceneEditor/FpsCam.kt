package pw.binom.sceneEditor

import mogot.math.*
import java.awt.event.MouseEvent

object FpsCamEditorFactory : EditActionFactory {
    override fun mouseDown(view: SceneEditorView, e: MouseEvent) {
        if (e.button == 3)
            view.startEditor(FpsCam(view))
    }
}

class FpsCam(val view: SceneEditorView) : EditAction {

    val normalSpeed = 6f
    val fastSpeed = 12f

    private var x = 0f
    private var y = 0f
    private var z = 0f
    val e = Vector3f()

    init {
        view.lockMouse = true
        view.cursorVisible = false

        view.editorCamera.quaternion.getEulerAnglesXYZ(e)
        y = e.x
        x = e.y
        z = e.z
    }

    override fun mouseUp(e: MouseEvent) {
        if (e.button == 3) {
            view.stopEditing()
            view.lockMouse = false
            view.cursorVisible = true
        }
    }

    private val keyDown = HashSet<Int>()
    override fun keyDown(code: Int) {
        println("code=$code")
        keyDown.add(code)
    }

    override fun keyUp(code: Int) {
        keyDown.remove(code)
    }

    override fun render(dt: Float) {
        val node = view.editorCamera
        val delta = dt

        val moveSpeed = if (16 in keyDown) fastSpeed else normalSpeed

        if (87 in keyDown) {
            node.position.add(node.quaternion.forward * delta * moveSpeed)
//            node.position.add(node.rotation.forward * dt * moveSpeed)
        }

        if (83 in keyDown) {
            node.position.add(node.quaternion.forward * delta * -moveSpeed)
//            node.position.add(-node.rotation.forward * dt * moveSpeed)
        }

        if (68 in keyDown) {
            node.position.add(-node.quaternion.left * delta * moveSpeed)
        }

        if (65 in keyDown) {
            node.position.add(node.quaternion.left * delta * moveSpeed)
        }

        if (69 in keyDown) {
            node.position.add(node.quaternion.up * moveSpeed * delta)
        }

        if (81 in keyDown) {
            node.position.add(-node.quaternion.up * moveSpeed * delta)
        }
        val centerX = view.size.x / 2
        val centerY = view.size.y / 2


        x += (view.mousePosition.x - centerX) * delta / 5f * (if (z <= -PIf / 2f || z >= PIf / 2f) -1f else 1f)
        y += (view.mousePosition.y - centerY) * delta / 5f

        node.quaternion.identity()
        println("rotateXYZ($y, $x)    init: $e")
        node.quaternion.rotateXYZ(y, x, z)
//            node.quaternion.rotateXYZ(x,y,0f)
//            node.rotation2.y += (Input.mousePosition.x - oldMousePosition.x) * dt / 10f
//            node.rotation2.x += (Input.mousePosition.y - oldMousePosition.y) * dt / 10f
    }
//
//    override fun onUpdate(delta: Float) {
//        engine.stage.lockMouse = engine.stage.isMouseDown(1)
//    }
}