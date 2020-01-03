package pw.binom.sceneEditor

import mogot.*
import mogot.Spatial
import mogot.math.right
import mogot.math.times
import mogot.math.*
import mogot.CSGBox
import mogot.math.up
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent
import mogot.collider.PanelCollider

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

    protected open fun stopEdit() {
        view.stopEditing()
        view.lockMouse = false
        view.cursorVisible = true
    }

    override fun keyUp(code: Int) {
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

    private val grid = Grid(view.engine)
    private val collader = PanelCollider(100f, 100f)
    private val box = CSGBox(view.engine)

    val node = (selected[0] as Spatial)

    init {
        collader.node = grid
        grid.parent = view.editorRoot
        grid.position.set(node.position)
        grid.material.value = view.default3DMaterial
        box.parent = view.editorRoot
        box.material.value = view.default3DMaterial
        box.width = 1.1f
        box.height = 1.1f
        box.depth = 1.1f
        view.lockMouse = false
        view.cursorVisible = true
    }

    override fun stopEdit() {
        grid.parent = null
        grid.close()
        box.parent = null
        box.close()
        super.stopEdit()
    }

    override fun render(dt: Float) {
//        view.editorCamera.position.set(10f, 10f, 10f)
//        view.editorCamera.lookTo(Vector3f(0f, 0f, 0f))
        val x = view.mousePosition.x
        val y = view.mousePosition.y
        val ray = MutableRay()
//        view.editorCamera.screenPointToRay(view.editorCamera.width / 2, view.editorCamera.height / 2, ray)
        view.editorCamera.screenPointToRay(x, y, ray)
        collader.rayCast(ray, box.position)
        println("-----------------------\nRay: $ray\nPosition=${box.position}\n-----------------------")
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
