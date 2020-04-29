package pw.binom.sceneEditor.editors

import mogot.math.*
import pw.binom.io.Closeable
import pw.binom.sceneEditor.EditAction
import pw.binom.sceneEditor.SceneEditorView
import java.awt.event.MouseEvent

object FpsCamEditorFactory : EditActionFactory {
    override fun mouseDown(view: SceneEditorView, e: MouseEvent) {
        if (e.button == 3 && view.mode == SceneEditorView.Mode.D3)
            view.startEditor(FpsCam(view))
    }
}

private class FpsManager : Closeable {
    var x = 0f
    var y = 0f
    override fun close() {
    }
}

class FpsCam(val view: SceneEditorView) : EditAction {

    val normalSpeed = 6f
    val fastSpeed = 12f
    private val manager = view.engine.manager("FpsManager") { FpsManager() }

    private val temp = Vector3f()

    //    private val q = Quaternionf()
    init {
        view.lockMouse = true
        view.cursorVisible = false
    }

    /*
        init {

    //        q.set(view.editorCamera.quaternion)
            val v = Vector3f()
            v.set(0f, 0f, 1f)
            //view.editorCamera.quaternion.getEulerAnglesXYZ(v)
            view.editorCamera.quaternion.mul(v, v)
            x = atan2(v.x, v.z)

    //        v.set(1f, 0f, 0f)
    //        view.editorCamera.quaternion.mul(v, v)
            val vv = v.cross(Vector3fc.UP, Vector3f())
            y = atan2(v.length, v.y)-PIf/2f

            println("y=${toDegrees(y)}")
            //y=v.y
    /*
            y = view.editorCamera.quaternion.roll
            x = view.editorCamera.quaternion.pitch

            x = -v.y
            //y = -v.z
            v.set(0f, 0f, 1f)
            view.editorCamera.quaternion.mul(v, v)
            x = atan2(v.x, v.z)
            if (view.editorCamera.quaternion.yaw > PIf * 0.5f)
                y = PIf - y
            println("-->${view.editorCamera.quaternion.yaw}             ${view.editorCamera.quaternion.pitch}")

    //        v.dot
    //        view.editorCamera.quaternion.mul(v, v)
    //        y = atan2(v.x, v.y)
     */
        }
    */
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

        if (Keys.W in keyDown) {
            node.position.add(temp)
        }

        if (Keys.S in keyDown) {
            temp.negated(temp)
            node.position.add(temp)
        }


        temp.set(delta * moveSpeed, 0f, 0f)
        node.quaternion.mul(temp, temp)
        if (Keys.D in keyDown) {
            node.position.add(temp)
        }

        if (Keys.A in keyDown) {
            temp.negated(temp)
            node.position.add(temp)
        }


        temp.set(0f, moveSpeed * delta, 0f)
        node.quaternion.mul(temp, temp)
        if (Keys.E in keyDown) {
            node.position.add(temp)
        }

        if (Keys.Q in keyDown) {
            temp.negated(temp)
            node.position.add(temp)
        }
        val centerX = view.size.x / 2
        val centerY = view.size.y / 2


        val dx = centerX - view.mousePosition.x
        val dy = centerY - view.mousePosition.y
        manager.x += (dx) * 0.005f
        manager.y += (dy) * 0.005f

        if (manager.y < -PIf / 2) manager.y = -PIf / 2
        if (manager.y > PIf / 2) manager.y = PIf / 2

        if (manager.x > PIf) manager.x = -PIf
        if (manager.x < -PIf) manager.x = PIf


        //println("$y")
        //node.quaternion.identity()
//        q.identity()
//        q.rotateZYX(0f,x,y)
//        node.quaternion.mul(q,node.quaternion)
//        node.quaternion.rotateZYX(0f, x, y)
//        println("-->${view.editorCamera.quaternion.yaw}             ${view.editorCamera.quaternion.pitch} $y")
        if (dx != 0 || dy != 0)
            node.quaternion.setRotation(0f, manager.x, manager.y)
//            node.quaternion.rotateXYZ(x,y,0f)
//            node.rotation2.y += (Input.mousePosition.x - oldMousePosition.x) * dt / 10f
//            node.rotation2.x += (Input.mousePosition.y - oldMousePosition.y) * dt / 10f
    }
//
//    override fun onUpdate(delta: Float) {
//        engine.stage.lockMouse = engine.stage.isMouseDown(1)
//    }
}