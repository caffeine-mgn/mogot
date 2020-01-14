package pw.binom.sceneEditor

import mogot.math.*
import pw.binom.sceneEditor.editors.EditActionFactory
import java.awt.event.MouseEvent

object FpsCamEditorFactory : EditActionFactory {
    override fun mouseDown(view: SceneEditorView, e: MouseEvent) {
        if (e.button == 3)
            view.startEditor(FpsCam(view))
    }
}

const val KEY_W = 87
const val KEY_S = 83
const val KEY_D = 68
const val KEY_A = 65
const val KEY_E = 69
const val KEY_Q = 81

class FpsCam(val view: SceneEditorView) : EditAction {

    val normalSpeed = 6f
    val fastSpeed = 12f

    private var x = 0f
    private var y = 0f
    private val temp = Vector3f()

    init {
        view.lockMouse = true
        view.cursorVisible = false


        y = -view.editorCamera.quaternion.roll
        x = -view.editorCamera.quaternion.pitch
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
        keyDown.add(code)
    }

    override fun keyUp(code: Int) {
        keyDown.remove(code)
    }

    override fun render(dt: Float) {
        val node = view.editorCamera
        val delta = dt

        val moveSpeed = if (16 in keyDown) fastSpeed else normalSpeed

        temp.set(0f, 0f, -(delta * moveSpeed))
        node.quaternion.mul(temp, temp)

        if (KEY_W in keyDown) {
            node.position.add(temp)
        }

        if (KEY_S in keyDown) {
            temp.negated(temp)
            node.position.add(temp)
        }


        temp.set(delta * moveSpeed, 0f, 0f)
        node.quaternion.mul(temp, temp)
        if (KEY_D in keyDown) {
            node.position.add(temp)
        }

        if (KEY_A in keyDown) {
            temp.negated(temp)
            node.position.add(temp)
        }


        temp.set(0f, moveSpeed * delta, 0f)
        node.quaternion.mul(temp, temp)
        if (KEY_E in keyDown) {
            node.position.add(temp)
        }

        if (KEY_Q in keyDown) {
            temp.negated(temp)
            node.position.add(temp)
        }
        val centerX = view.size.x / 2
        val centerY = view.size.y / 2


        x += (view.mousePosition.x - centerX) * 0.005f
        y += (view.mousePosition.y - centerY) * 0.005f

        node.quaternion.setRotation(0f, -x, -y)
//            node.quaternion.rotateXYZ(x,y,0f)
//            node.rotation2.y += (Input.mousePosition.x - oldMousePosition.x) * dt / 10f
//            node.rotation2.x += (Input.mousePosition.y - oldMousePosition.y) * dt / 10f
    }
//
//    override fun onUpdate(delta: Float) {
//        engine.stage.lockMouse = engine.stage.isMouseDown(1)
//    }
}