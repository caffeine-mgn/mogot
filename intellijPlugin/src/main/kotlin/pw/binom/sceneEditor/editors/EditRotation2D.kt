package pw.binom.sceneEditor.editors

import mogot.Camera2D
import mogot.Node
import mogot.Spatial2D
import mogot.math.*
import pw.binom.sceneEditor.Line2D
import pw.binom.sceneEditor.SceneEditorView
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent
import kotlin.math.atan2

object EditRotate2DFactory : EditActionFactory {
    override fun keyDown(view: SceneEditorView, e: KeyEvent) {
        if (e.keyCode == Keys.R && view.mode == SceneEditorView.Mode.D2) {
            val nodes = view.selected.mapNotNull { it as? Spatial2D }
            if (nodes.isNotEmpty())
                view.startEditor(EditRotateAllAxes2D(
                        view = view,
                        root = view.editorRoot,
                        camera = view.editorCamera2D,
                        selected = nodes))
        }
    }
}

abstract class EditRotateEditor2D(view: SceneEditorView, val root: Node, selected: List<Spatial2D>) : Spatial2DEditor(view, selected) {
    val screenPos = view.editorCamera2D.worldToScreen(avgPosition)

    protected var totalRotation = 0f
        private set

    private val oldVirtualMouse = Vector2i(virtualMouse)
    private var startAngle = angle1()
    private var startAngle2 = angle2()

    private fun angle1(): Float {
        return atan2(virtualMouse.y.toFloat() - screenPos.y, virtualMouse.x.toFloat() - screenPos.x)
    }

    private fun angle2(): Float {
        return atan2(virtualMouse.x.toFloat() - screenPos.x, -virtualMouse.y.toFloat() - screenPos.y)
    }


    protected val center = Line2D(engine).also {
        it.parent = root
        it.material = view.default3DMaterial.instance(Vector4f(1f))
        it.position.set(avgPosition)
    }


    override fun render(dt: Float) {
        oldVirtualMouse.set(virtualMouse)
        super.render(dt)
        //center.position.set(avgPosition)
        val tmp = view.engine.mathPool.vec2f.poll()
        view.editorCamera2D.screenToWorld(virtualMouse, tmp)
        center.lineTo.set(tmp.sub(avgPosition))
        view.engine.mathPool.vec2f.push(tmp)

        val angle = angle1()//atan2(virtualMouse.y.toFloat() - screenPos.y, virtualMouse.x.toFloat() - screenPos.x)
        val angle2 = angle2()//atan2(virtualMouse.x.toFloat() - screenPos.x, -virtualMouse.y.toFloat() - screenPos.y)

        val r = if (angle.isPositive != startAngle.isPositive && angle2.isPositive == startAngle2.isPositive) {
            startAngle2 - angle2
        } else {
            startAngle - angle
        }

        startAngle = angle
        startAngle2 = angle2

        totalRotation -= if (slow) r * 0.1f else r
    }

    override fun onStop() {
        center.parent = null
        view.renderThread {
            center.close()
        }
    }
}

@Strictfp
class EditRotateAllAxes2D(view: SceneEditorView, root: Node, camera: Camera2D, selected: List<Spatial2D>) : EditRotateEditor2D(view, root, selected) {
    private val tempMatrix = Matrix4f()
    private val globalRotation = Quaternionf()
    private val newRotation = Quaternionf()
    private val oldScale = Vector3f()
    override fun render(dt: Float) {
        super.render(dt)
        if (initPositions.isEmpty())
            TODO()

        if (initPositions.size == 1)
            initPositions.forEach { (node, matrix) ->
                val m = tempMatrix.identity().rotateZ(totalRotation)
                        .mul(matrix)
                        .setTranslation(matrix.getTranslation(Vector3f()))
                node.setGlobalTransform(m)
            }
        else {
            globalRotation.identity()
//        val axis = camera.quaternion.mul(Vector3fc.Z, Vector3f())
            globalRotation.rotateZYX(totalRotation, 0f, 0f)
            initPositions.forEach { (node, matrix) ->
                val avr3 = Vector3f(avgPosition.x, avgPosition.y, 0f)
                val newPosition = globalRotation.mul(matrix.getTranslation(Vector3f()).sub(avr3)).add(avr3)
                matrix.getScale(oldScale)
                newRotation.identity().setFromUnnormalized(matrix).let { globalRotation.mul(it, it) }
                val newMatrix = tempMatrix.identity()
                        .translationRotateScale(newPosition, newRotation, oldScale)
                node.setGlobalTransform(newMatrix)
            }
        }
        updatePropertyPosition()
    }

    /*
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
    */
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