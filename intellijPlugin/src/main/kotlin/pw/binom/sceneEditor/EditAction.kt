package pw.binom.sceneEditor

import mogot.Node
import mogot.Spatial
import mogot.collider.PanelCollider
import mogot.isSpatial
import mogot.math.*
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

    protected val node = selected.filter { it.isSpatial }.map { it as Spatial }
    protected val avgVec: Vector3f

    init {
        val tmpVec = Vector3f()
        val vec = Vector3f()
        node.forEach {
            tmpVec.set(0f)
            it.localToGlobal(tmpVec, tmpVec)
            vec += tmpVec
        }
        avgVec = vec / node.size.toFloat()
    }

    private val oldPositions = selected.asSequence()
            .filter { it.isSpatial }
            .map { it as Spatial }
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
//        view.lockMouse = true
//        view.cursorVisible = false
    }
}

class EditMoveOneAxis(view: SceneEditorView, selected: List<Node>, val type: EditMoveOneAxis.Type) : EditMove(view, selected) {
    enum class Type {
        X, Y, Z
    }

    protected val grid = Line(view.engine)
    protected val collader = PanelCollider(100f, 100f)
    protected val ray = MutableRay()
    protected val vec = Vector3f()
    private var startValue: Float

    init {
        grid.parent = view.editorRoot
        grid.position.set(avgVec)
        grid.material.value = view.default3DMaterial
        collader.node = grid
    }

    init {
        if (type == Type.Y) {
            grid.quaternion.rotateXYZ(0f, 0f, -PIf / 2f)
        }

        if (type == Type.Z) {
            grid.quaternion.rotateXYZ(0f, -PIf / 2f, 0f)
        }
        view.editorCamera.screenPointToRay(view.mousePosition.x, view.mousePosition.y, ray)
        collader.rayCast(ray, vec)
        startValue = when (type) {
            EditMoveOneAxis.Type.X -> vec.x
            EditMoveOneAxis.Type.Y -> vec.y
            EditMoveOneAxis.Type.Z -> vec.z
        }
    }

    override fun stopEdit() {
        grid.parent = null
        grid.close()
        super.stopEdit()
    }

    override fun render(dt: Float) {
        view.editorCamera.screenPointToRay(view.mousePosition.x, view.mousePosition.y, ray)
        collader.rayCast(ray, vec)
        val value = when (type) {
            Type.X -> vec.x
            Type.Y -> vec.y
            Type.Z -> vec.z
        }
        if (value == startValue) {
            return
        }

        val pos = Vector3f()
        node.forEach {
            pos.set(it.position)
            it.parentSpatial?.localToGlobal(pos, pos)
            when (type) {
                Type.X -> pos.x += value - startValue
                Type.Y -> pos.y += startValue - value
                Type.Z -> pos.z += startValue - value
            }
            it.parentSpatial?.globalToLocal(pos, pos)
            it.position.set(pos)
        }
        startValue = value
    }
}

/*
class EditMoveYAxie(view: SceneEditorView, selected: List<Node>) : EditMoveOneAxis(view, selected) {
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
*/
class EditMoveAllAxie(view: SceneEditorView, selected: List<Node>) : EditMove(view, selected) {

    override fun keyDown(code: Int) {
        if (code == 88) {
            resetPositions()
            view.stopEditing()
            view.startEditor(EditMoveOneAxis(view, selected, EditMoveOneAxis.Type.X))
        }

        if (code == 89) {
            resetPositions()
            view.stopEditing()
            view.startEditor(EditMoveOneAxis(view, selected, EditMoveOneAxis.Type.Y))
        }

        if (code == 90) {
            resetPositions()
            view.stopEditing()
            view.startEditor(EditMoveOneAxis(view, selected, EditMoveOneAxis.Type.Z))
        }
//        super.keyDown(code)
    }

    private var oldX = view.mousePosition.x
    private var oldY = view.mousePosition.y

    override fun render(dt: Float) {
        val x = view.mousePosition.x
        val y = view.mousePosition.y

        val yd = view.editorCamera.quaternion.up * (y - oldY).toFloat() * -0.01f
        val xd = view.editorCamera.quaternion.right * (x - oldX).toFloat() * 0.01f
        oldX = x
        oldY = y

        selected.asSequence().map { it as? Spatial }.filterNotNull().forEach {
            it.position += xd
            it.position += yd
        }
    }

}
