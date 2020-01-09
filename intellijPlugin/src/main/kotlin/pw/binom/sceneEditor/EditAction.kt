package pw.binom.sceneEditor

import mogot.Node
import mogot.Spatial
import mogot.collider.PanelCollider
import mogot.isSpatial
import mogot.math.*
import mogot.*
import pw.binom.SimpleMaterial
import pw.binom.sceneEditor.properties.PositionProperty
import java.awt.MouseInfo
import java.awt.Robot
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent
import kotlin.math.atan2

interface EditAction {
    fun keyDown(code: Int) {}
    fun keyUp(code: Int) {}
    fun mouseDown(e: MouseEvent) {}
    fun mouseUp(e: MouseEvent) {}
    fun render(dt: Float) {}
}

class RotateAllAxes(engine: Engine, editorRoot: Node, val camera: Camera, val selected: List<Spatial>, val mousePos: Vector2i) : EditAction {
    val initPositions = selected.asSequence().map {
        it to it.localToGlobalMatrix(Matrix4f())
    }.toMap()
    val avgPosition = selected.asSequence().map { it.position }.avg()
    val screenPos = camera.worldToScreenPoint(avgPosition) ?: TODO()
    lateinit var s: Grid
    lateinit var v: CSGBox
    var mx = mousePos.x
    var my = mousePos.y
    var vmx = mousePos.x
    var vmy = mousePos.y
    val imx = mousePos.x
    val imy = mousePos.y
    val startAngle = atan2(mousePos.y.toFloat() - screenPos.y, mousePos.x.toFloat() - screenPos.x)

    init {

    }

    override fun render(dt: Float) {
        super.render(dt)
        vmx += mousePos.x - mx
        vmy += mousePos.y - my
        mx = mousePos.x
        my = mousePos.y

        val angle = atan2(vmy.toFloat() - screenPos.y, vmx.toFloat() - screenPos.x)

        val rotateAngle = startAngle - angle
        val q = Quaternionf()
        val axis = camera.quaternion.mul(Vector3f(0f, 0f, 1f), Vector3f())
        q.rotateAxis(rotateAngle, axis.x, axis.y, axis.z)
        selected.forEach {
            it.setGlobalTransform(initPositions[it]!!.rotate(q, Matrix4f()))
            //q.mul(it.quaternion,it.quaternion)
        }
        println("${toDegrees(rotateAngle)}")
        //val camPosition = camera.globalToLocal(Vector3f(0f, 0f, 0f), Vector3f())

//        v.position.set(camera.position)
//        v.position.y-=3f
//        v.position.set(3f, 0f, 0f)
//        v.quaternion.identity()
//        v.quaternion.lookAlong(-camera.quaternion.forward, Vector3fc.UP)
//        s.position.set(camPosition)
//        s.position *= camera.quaternion.forward
    }
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

private fun SceneEditorView.updatePropertyPosition() {
    editor1.propertyTool.properties
            .mapNotNull { it as? PositionProperty }
            .forEach {
                it.update()
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
    private var slow = false

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
        view.renderThread {
            grid.close()
        }
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
        val cof = if (slow) 0.1f else 1f
        node.forEach {
            pos.set(it.position)
            it.parentSpatial?.localToGlobal(pos, pos)
            when (type) {
                Type.X -> pos.x += (value - startValue) * cof
                Type.Y -> pos.y += (startValue - value) * cof
                Type.Z -> pos.z += (value - startValue) * cof
            }
            it.parentSpatial?.globalToLocal(pos, pos)
            it.position.set(pos)
        }
        view.updatePropertyPosition()
        startValue = value
    }

    override fun keyDown(code: Int) {
        if (code == 16) {
            slow = true
            return
        }
        super.keyDown(code)
    }

    override fun keyUp(code: Int) {
        if (code == 16) {
            slow = false
            return
        }
        super.keyUp(code)
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

class MouseMoveReset(val view: SceneEditorView) {
    val robot = Robot()

    fun check(): Vector2i? {
        var needRefresh = false
        val viewLocation = view.locationOnScreen
        val mouseLocation = MouseInfo.getPointerInfo().location
        var x = mouseLocation.x
        var y = mouseLocation.y
        if (mouseLocation.x < viewLocation.x) {
            x = viewLocation.x + view.width
            needRefresh = true
        }

        if (mouseLocation.x > viewLocation.x + view.width) {
            x = viewLocation.x
            needRefresh = true
        }

        if (mouseLocation.y < viewLocation.y) {
            y = viewLocation.y + view.height
            needRefresh = true
        }

        if (mouseLocation.y > viewLocation.y + view.height) {
            y = viewLocation.y
            needRefresh = true
        }

        if (needRefresh) {
            println("mouse=${mouseLocation} view=${viewLocation} (${view.width} x ${view.height})")
            robot.mouseMove(x, y)
            return Vector2i(x, y)
        }
        return null
    }
}

class EditMoveAllAxie(view: SceneEditorView, selected: List<Node>) : EditMove(view, selected) {
    private val mouseMoveReset = MouseMoveReset(view)
    override fun keyDown(code: Int) {
        if (code == 88) {
            resetPositions()
            view.stopEditing()
            view.startEditor(EditMoveOneAxis(view, selected, EditMoveOneAxis.Type.X))
            return
        }

        if (code == 89) {
            resetPositions()
            view.stopEditing()
            view.startEditor(EditMoveOneAxis(view, selected, EditMoveOneAxis.Type.Y))
            return
        }

        if (code == 90) {
            resetPositions()
            view.stopEditing()
            view.startEditor(EditMoveOneAxis(view, selected, EditMoveOneAxis.Type.Z))
            return
        }
        if (code == 16) {
            slow = true
            return
        }
        println("code=$code")
        super.keyDown(code)
    }

    override fun keyUp(code: Int) {
        if (code == 16) {
            slow = false
            return
        }
        super.keyUp(code)
    }

    private var oldX = MouseInfo.getPointerInfo().location.x
    private var oldY = MouseInfo.getPointerInfo().location.y
    private var slow = false

    override fun render(dt: Float) {
        val x: Int
        val y: Int
        val resetPos = mouseMoveReset.check()
        if (resetPos != null) {
            oldX = resetPos.x
            oldY = resetPos.y
            x = resetPos.x
            y = resetPos.y
        } else {
            val loc = MouseInfo.getPointerInfo().location
            x = loc.x
            y = loc.y
        }


        val cof = if (slow) 0.01f else 0.05f
        println("cof=$cof")
        val yd = view.editorCamera.quaternion.up * (oldY - y).toFloat() * cof
        val xd = view.editorCamera.quaternion.right * (x - oldX).toFloat() * cof
        oldX = x
        oldY = y

        selected.asSequence()
                .map { it as? Spatial }
                .filterNotNull()
                .forEach {
                    it.position += xd
                    it.position += yd
                }
        view.updatePropertyPosition()
    }

}


fun Sequence<Vector3fc>.avg(): Vector3f {
    val out = Vector3f()
    var count = 0
    forEach {
        out.add(it)
        count++
    }
    if (count == 0)
        return out
    out.x = if (out.x == 0f) 0f else out.x / count.toFloat()
    out.y = if (out.y == 0f) 0f else out.y / count.toFloat()
    out.z = if (out.z == 0f) 0f else out.z / count.toFloat()
    return out
}