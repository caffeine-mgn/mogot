package pw.binom.sceneEditor.editors

import mogot.math.Vector2i
import mogot.math.add
import mogot.math.set
import pw.binom.sceneEditor.EditAction
import pw.binom.sceneEditor.SceneEditorView

abstract class EditorWithVirtualMouse(val view: SceneEditorView) : EditAction {
    protected val engine
        get() = view.engine

    private var oldMousePosition = Vector2i(engine.stage.mousePosition)
    protected var virtualMouse = Vector2i(engine.stage.mousePosition)
    private val mouseMoveResetUtil = MouseMoveResetUtil(view)

    override fun render(dt: Float) {
        val newPos = mouseMoveResetUtil.check()
        if (newPos == null) {
            virtualMouse.add(
                    engine.stage.mousePosition.x - oldMousePosition.x,
                    engine.stage.mousePosition.y - oldMousePosition.y
            )
            oldMousePosition.set(
                    engine.stage.mousePosition.x,
                    engine.stage.mousePosition.y
            )
        } else {
            oldMousePosition.set(newPos)
        }
    }

    override fun keyDown(code: Int) {
        if (code == Keys.SHIFT)
            slow = true
        else
            super.keyDown(code)
    }

    override fun keyUp(code: Int) {
        if (code == Keys.SHIFT)
            slow = false
        else
            super.keyUp(code)
    }

    var slow = false
        private set
    //get() = engine.stage.isKeyDown(Keys.SHIFT)

    abstract fun resetInitPosition()
}