package pw.binom.sceneEditor

import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.command.UndoConfirmationPolicy
import com.intellij.openapi.command.WriteCommandAction
import mogot.Node
import mogot.Spatial
import mogot.math.right
import mogot.math.times
import mogot.math.up
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent

interface EditAction {
    fun keyDown(code: Int) {}
    fun keyUp(code: Int) {}
    fun mouseDown(e: MouseEvent) {}
    fun mouseUp(e: MouseEvent) {}
    fun render(dt: Float) {}
}

interface EditActionFactory {
    fun mouseDown(view: SceneEditorView, e: MouseEvent) {}
    fun keyDown(view: SceneEditorView, e: KeyEvent) {}
}

object EditMoveFactory : EditActionFactory {
    override fun keyDown(view: SceneEditorView, e: KeyEvent) {
        if (e.keyCode == 71) {
            view.startEditor(EditMoveAllAxie(view, view.selected))
        }
    }
}

abstract class EditMove(val view: SceneEditorView, val selected: List<Node>) : EditAction {

    private val oldPositions = selected.asSequence()
            .map { it as? Spatial }
            .filterNotNull()
            .associateWith { it.position.copy() }

    protected fun resetPositions() {
        oldPositions.forEach { (t, u) ->
            t.position.set(u)
        }
    }

    protected fun stopEdit() {
        view.stopEditing()
        view.lockMouse = false
        view.cursorVisible = true
    }

    override fun keyUp(code: Int) {
        println("code=$code")
        if (code == 27) {
            resetPositions()
            stopEdit()
        }
        if (code == 10) {
            stopEdit()
        }
    }

    override fun mouseDown(e: MouseEvent) {
        if (e.button == 1) {
            stopEdit()
        }

        if (e.button == 3) {
            resetPositions()
            stopEdit()
        }
    }

    init {
        view.lockMouse = true
        view.cursorVisible = false
    }
}

class EditMoveXAxie(view: SceneEditorView, selected: List<Node>) : EditMove(view, selected) {
    override fun render(dt: Float) {
        val x = view.mousePosition.x - view.size.x / 2

        selected.asSequence().map { it as? Spatial }.filterNotNull().forEach {
            it.position.x += x * 0.01f
        }
    }
}

class EditMoveYAxie(view: SceneEditorView, selected: List<Node>) : EditMove(view, selected) {
    override fun render(dt: Float) {
        val y = view.mousePosition.y - view.size.y / 2

        selected.asSequence().map { it as? Spatial }.filterNotNull().forEach {
            it.position.y -= y * 0.01f
        }
    }
}

class EditMoveZAxie(view: SceneEditorView, selected: List<Node>) : EditMove(view, selected) {
    override fun render(dt: Float) {
        val y = view.mousePosition.y - view.size.y / 2

        selected.asSequence().map { it as? Spatial }.filterNotNull().forEach {
            it.position.z -= y * 0.01f
        }
    }
}

class EditMoveAllAxie(view: SceneEditorView, selected: List<Node>) : EditMove(view, selected) {

    override fun keyDown(code: Int) {
        if (code == 88) {
            resetPositions()
            view.stopEditing()
            view.startEditor(EditMoveXAxie(view, selected))
        }
        if (code == 89) {
            resetPositions()
            view.stopEditing()
            view.startEditor(EditMoveYAxie(view, selected))
        }

        if (code == 90) {
            resetPositions()
            view.stopEditing()
            view.startEditor(EditMoveZAxie(view, selected))
        }
//        super.keyDown(code)
    }

    override fun render(dt: Float) {
        val x = view.mousePosition.x - view.size.x / 2
        val y = view.mousePosition.y - view.size.y / 2

        val yd = view.editorCamera.quaternion.up * y.toFloat() * -0.01f
        val xd = view.editorCamera.quaternion.right * x.toFloat() * 0.01f

        selected.asSequence().map { it as? Spatial }.filterNotNull().forEach {
            it.position += xd
            it.position += yd
        }
    }

}
