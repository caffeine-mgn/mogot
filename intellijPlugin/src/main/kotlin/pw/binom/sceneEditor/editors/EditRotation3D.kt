package pw.binom.sceneEditor.editors

import mogot.Camera
import mogot.Node
import mogot.Spatial
import mogot.math.*
import pw.binom.sceneEditor.*
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent
import kotlin.math.atan2

object EditRotate3DFactory : EditActionFactory {
    override fun keyDown(view: SceneEditorView, e: KeyEvent) {
        if (e.keyCode == Keys.R && view.mode == SceneEditorView.Mode.D3) {
            val nodes = view.selected.mapNotNull { it as? Spatial }
            if (nodes.isNotEmpty())
                view.startEditor(EditRotateAllAxes3D(
                        view = view,
                        root = view.editorRoot,
                        camera = view.editorCamera,
                        selected = nodes))
        }
    }
}

abstract class EditRotateEditor3D(view: SceneEditorView, val root: Node, val camera: Camera, selected: List<Spatial>) : SpatialEditor(view, selected) {
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


    protected val center = Line2D(engine).also {
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

@Strictfp
class EditRotateOneAxis3D(view: SceneEditorView, root: Node, camera: Camera, selected: List<Spatial>, val axis: Axis) : EditRotateEditor3D(view, root, camera, selected) {
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

    private val tempMatrix = Matrix4f()
    private val globalRotation = Quaternionf()
    private val newRotation = Quaternionf()
    private val axisVec = Vector3f()
    private val oldScale = Vector3f()
    override fun render(dt: Float) {
        super.render(dt)
        globalRotation.identity()
        val axis = when (axis) {
            Axis.X -> {
                val rotateDirection = if (camera.position.x - avgPosition.x > 0f) 1f else -1f
                globalRotation.mul(axisVec.set(rotateDirection, 0f, 0f))
            }
            Axis.Y -> {
                val rotateDirection = if (camera.position.y - avgPosition.y > 0f) 1f else -1f
                globalRotation.mul(axisVec.set(0f, rotateDirection, 0f))
            }
            Axis.Z -> {
                val rotateDirection = if (camera.position.z - avgPosition.z > 0f) 1f else -1f
                globalRotation.mul(axisVec.set(0f, 0f, rotateDirection))
            }
        }
        globalRotation.rotateAxis(totalRotation, axis.x, axis.y, axis.z)

        if (initPositions.size == 1)
            initPositions.forEach {
                val m = tempMatrix.identity().rotate(globalRotation)
                        .mul(it.value)
                        .setTranslation(it.value.getTranslation(Vector3f()))
                it.key.setGlobalTransform(m)
            }
        else
            initPositions.forEach { (node, matrix) ->
                val newPosition = globalRotation.mul(matrix.getTranslation(Vector3f()).sub(avgPosition)).add(avgPosition)
                matrix.getScale(oldScale)
                newRotation.identity().setFromUnnormalized(matrix).let { globalRotation.mul(it, it) }
                val newMatrix = tempMatrix.identity()
                        .translationRotateScale(newPosition, newRotation, oldScale)
                node.setGlobalTransform(newMatrix)
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

@Strictfp
class EditRotateAllAxes3D(view: SceneEditorView, root: Node, camera: Camera, selected: List<Spatial>) : EditRotateEditor3D(view, root, camera, selected) {
    private val tempMatrix = Matrix4f()
    private val globalRotation = Quaternionf()
    private val newRotation = Quaternionf()
    private val oldScale = Vector3f()
    override fun render(dt: Float) {
        super.render(dt)
        if (initPositions.isEmpty())
            TODO()

        globalRotation.identity()
        val axis = camera.quaternion.mul(Vector3fc.Z, Vector3f())
        globalRotation.rotateAxis(totalRotation, axis.x, axis.y, axis.z)

        if (initPositions.size == 1)
            initPositions.forEach { (node, matrix) ->
                val m = tempMatrix.identity().rotate(globalRotation)
                        .mul(matrix)
                        .setTranslation(matrix.getTranslation(Vector3f()))
                node.setGlobalTransform(m)
            }
        else
            initPositions.forEach { (node, matrix) ->
                val newPosition = globalRotation.mul(matrix.getTranslation(Vector3f()).sub(avgPosition)).add(avgPosition)
                matrix.getScale(oldScale)
                newRotation.identity().setFromUnnormalized(matrix).let { globalRotation.mul(it, it) }
                val newMatrix = tempMatrix.identity()
                        .translationRotateScale(newPosition, newRotation, oldScale)
                node.setGlobalTransform(newMatrix)
            }
    }

    override fun keyUp(code: Int) {
        when (code) {
            Keys.X -> {
                resetInitPosition()
                view.startEditor(EditRotateOneAxis3D(view, root, camera, selected, Axis.X))
            }
            Keys.Y -> {
                resetInitPosition()
                view.startEditor(EditRotateOneAxis3D(view, root, camera, selected, Axis.Y))
            }
            Keys.Z -> {
                resetInitPosition()
                view.startEditor(EditRotateOneAxis3D(view, root, camera, selected, Axis.Z))
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