package pw.binom.sceneEditor.editors

import mogot.Camera
import mogot.Engine
import mogot.Node
import mogot.Spatial
import mogot.math.*
import pw.binom.sceneEditor.*
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent
import kotlin.math.atan2

object RotateAllAxesFactory : EditActionFactory {
    override fun keyDown(view: SceneEditorView, e: KeyEvent) {
        if (e.keyCode == 82) {
            val nodes = view.selected.mapNotNull { it as? Spatial }
            if (nodes.isNotEmpty())
                view.startEditor(RotateAllAxes(
                        view = view,
                        root = view.editorRoot,
                        camera = view.editorCamera,
                        selected = nodes))
        }
    }
}

abstract class EditorWithVirtualMouse(val view: SceneEditorView) : EditAction {
    val engine
        get() = view.engine

    var oldMousePosition = Vector2i(engine.stage.mousePosition)
    var virtualMouse = Vector2i(engine.stage.mousePosition)
    val mouseMoveResetUtil = MouseMoveResetUtil(view)

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

abstract class SpatialEditor(view: SceneEditorView, val selected: List<Spatial>) : EditorWithVirtualMouse(view) {
    val initPositions = selected.asSequence().map {
        it to it.localToGlobalMatrix(Matrix4f())
    }.toMap()
    val avgPosition = selected.asSequence().map { it.position }.avg()

    override fun resetInitPosition() {
        initPositions.forEach { (node, matrix) ->
            node.setGlobalTransform(matrix)
        }
    }

    override fun keyUp(code: Int) {
        when (code) {
            Keys.ESCAPE -> {
                resetInitPosition()
                view.stopEditing()
            }
            Keys.ENTER -> {
                view.stopEditing()
            }
            else -> super.keyUp(code)
        }
    }

    override fun mouseUp(e: MouseEvent) {
        if (e.button == 1) {
            view.stopEditing()
        }

        if (e.button == 3) {
            resetInitPosition()
            view.stopEditing()
        }
    }
}

abstract class RotateEditor(view: SceneEditorView, val root: Node, val camera: Camera, selected: List<Spatial>) : SpatialEditor(view, selected) {
    val screenPos = camera.worldToScreenPoint(avgPosition) ?: TODO()

    protected var totalRotation = 0f

    private var startAngle = angle1()
    private var startAngle2 = angle2()

    fun angle1(): Float {
        return atan2(engine.stage.mousePosition.y.toFloat() - screenPos.y, engine.stage.mousePosition.x.toFloat() - screenPos.x)
    }

    fun angle2(): Float {
        return atan2(virtualMouse.x.toFloat() - screenPos.x, -virtualMouse.y.toFloat() - screenPos.y)
    }


    protected val center = Line2D(engine.gl).also {
        it.parent = root
        it.material = view.default3DMaterial
    }

    override fun render(dt: Float) {
        super.render(dt)
        center.position.set(screenPos.x.toFloat(), screenPos.y.toFloat())
        center.lineTo.set(
                virtualMouse.x.toFloat() - screenPos.x,
                virtualMouse.y.toFloat() - screenPos.y
        )

        val angle = angle1()//atan2(virtualMouse.y.toFloat() - screenPos.y, virtualMouse.x.toFloat() - screenPos.x)
        val angle2 = angle2()//atan2(virtualMouse.x.toFloat() - screenPos.x, -virtualMouse.y.toFloat() - screenPos.y)

        val r = if (angle.isPositive != startAngle.isPositive && angle2.isPositive == startAngle2.isPositive) {
            startAngle2 - angle2
        } else {
            startAngle - angle
        }
//        val r = startAngle - angle
        startAngle = angle
        startAngle2 = angle2

        totalRotation += if (slow) r * 0.1f else r
    }

    override fun onStop() {
        center.parent = null
        view.renderThread {
            center.close()
        }
    }
}

class RotateOneAxis(view: SceneEditorView, root: Node, camera: Camera, selected: List<Spatial>, val axis: Axis) : RotateEditor(view, root, camera, selected) {
    private val grid = Line(engine).also {
        it.parent = root
        it.material.value = view.default3DMaterial
        it.position.set(avgPosition)

        if (axis == Axis.Y) {
            it.quaternion.rotateXYZ(0f, 0f, -PIf / 2f)
        }

        if (axis == Axis.Z) {
            it.quaternion.rotateXYZ(0f, -PIf / 2f, 0f)
        }
    }

    override fun render(dt: Float) {
        super.render(dt)
        val q = Quaternionf()
        val axis = when (axis) {
            Axis.Z -> {
                val rotateDirection = if (camera.position.z - avgPosition.z > 0f) 1f else -1f
                q.mul(Vector3f(0f, 0f, rotateDirection), Vector3f())
            }
            Axis.X -> {
                val rotateDirection = if (camera.position.x - avgPosition.x > 0f) 1f else -1f
                q.mul(Vector3f(rotateDirection, 0f, 0f), Vector3f())
            }
            Axis.Y -> {
                val rotateDirection = if (camera.position.y - avgPosition.y > 0f) 1f else -1f
                q.mul(Vector3f(0f, rotateDirection, 0f), Vector3f())
            }
        }
        q.rotateAxis(totalRotation, axis.x, axis.y, axis.z)

        if (initPositions.size == 1)
            initPositions.forEach {
                val m = Matrix4f().rotate(q)
                        .mul(it.value)
                        .setTranslation(it.value.getTranslation(Vector3f()))
                it.key.setGlobalTransform(m)
            }
        else
            initPositions.forEach {
                val m = Matrix4f().rotate(q)
                        .mul(it.value)
                it.key.setGlobalTransform(m)
            }
    }

    override fun onStop() {
        super.onStop()
        grid.parent = null
        view.renderThread {
            grid.close()
        }
    }
}

class RotateAllAxes(view: SceneEditorView, root: Node, camera: Camera, selected: List<Spatial>) : RotateEditor(view, root, camera, selected) {

    override fun render(dt: Float) {
        super.render(dt)
        if (initPositions.isEmpty())
            TODO()

        val q = Quaternionf()
        val axis = camera.quaternion.mul(Vector3f(0f, 0f, 1f), Vector3f())
        q.rotateAxis(totalRotation, axis.x, axis.y, axis.z)

        if (initPositions.size == 1)
            initPositions.forEach {
                val m = Matrix4f().rotate(q)
                        .mul(it.value)
                        .setTranslation(it.value.getTranslation(Vector3f()))
                it.key.setGlobalTransform(m)
            }
        else
            initPositions.forEach {
                val m = Matrix4f().rotate(q)
                m.mul(it.value, m)
                it.key.setGlobalTransform(m)
            }
    }

    override fun keyUp(code: Int) {
        when (code) {
            Keys.X -> {
                resetInitPosition()
                view.startEditor(RotateOneAxis(view, root, camera, selected, Axis.X))
            }
            Keys.Y -> {
                resetInitPosition()
                view.startEditor(RotateOneAxis(view, root, camera, selected, Axis.Y))
            }
            Keys.Z -> {
                resetInitPosition()
                view.startEditor(RotateOneAxis(view, root, camera, selected, Axis.Z))
            }
            else -> super.keyUp(code)
        }
    }

    override fun mouseUp(e: MouseEvent) {
        if (e.button == 1) {
            view.stopEditing()
        }

        if (e.button == 3) {
            resetInitPosition()
            view.stopEditing()
        }
    }
}