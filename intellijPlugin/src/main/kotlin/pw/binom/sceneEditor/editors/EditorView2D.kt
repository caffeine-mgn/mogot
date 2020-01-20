package pw.binom.sceneEditor.editors

import mogot.math.Vector2i
import mogot.math.set
import pw.binom.sceneEditor.SceneEditorView
import java.awt.event.MouseEvent

object EditorView2DFactory : EditActionFactory {
    override fun mouseDown(view: SceneEditorView, e: MouseEvent) {
        if (e.button == 3 && view.mode == SceneEditorView.Mode.D2)
            view.startEditor(EditorView2D(view))
    }
}

class EditorView2D(view: SceneEditorView) : EditorWithVirtualMouse(view) {
    val oldMousePos = Vector2i(virtualMouse)
    override fun render(dt: Float) {
        val dx = virtualMouse.x - oldMousePos.x
        val dy = virtualMouse.y - oldMousePos.y
        oldMousePos.set(virtualMouse)
        view.editorCamera2D.position.x -= dx
        view.editorCamera2D.position.y -= dy
        super.render(dt)
    }

    override fun mouseUp(e: MouseEvent) {
        if (e.button == 3)
            view.stopEditing()
        super.mouseUp(e)
    }

    override fun resetInitPosition() {
    }
}