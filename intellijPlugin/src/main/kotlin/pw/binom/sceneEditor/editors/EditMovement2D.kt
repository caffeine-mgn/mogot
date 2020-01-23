package pw.binom.sceneEditor.editors

import mogot.Spatial2D
import mogot.math.*
import mogot.onlySpatial2D
import mogot.use1
import pw.binom.sceneEditor.SceneEditorView
import java.awt.event.KeyEvent


object EditMovementFactory2D : EditActionFactory {
    override fun keyDown(view: SceneEditorView, e: KeyEvent) {
        if (e.keyCode == Keys.G && view.mode == SceneEditorView.Mode.D2) {
            view.startEditor(EditMovement2D(view, view.selected.asSequence().onlySpatial2D().toList(), null))
        }
    }
}

abstract class AbstractEditMovement2D(view: SceneEditorView, selected: List<Spatial2D>) : Spatial2DEditor(view, selected) {

    protected open fun stopEdit() {
        view.stopEditing()
        view.lockMouse = false
        view.cursorVisible = true
    }
}

/*
@Strictfp
class EditMovementOneAxis2D(view: SceneEditorView, selected: List<Spatial>, val type: Axis) : EditMovement3D(view, selected) {

    private val axisLine = Line(view.engine)
    private val collader = Panel3DCollider(view.editorCamera.far * 2f, view.editorCamera.far * 2f)
    private val ray = MutableRay()
    private val vec = Vector3f()
    private val startVec = Vector3f()

    private val old = Vector2i(virtualMouse)
    private var moveDirection = Vector2f(virtualMouse.x.toFloat(), virtualMouse.y.toFloat())

    init {
        axisLine.parent = view.editorRoot
        axisLine.position.set(avgPosition)
        axisLine.material.value = view.default3DMaterial
        collader.node = axisLine
    }

    init {
        when (type) {
            Axis.X -> {
                axisLine.axis = Axis.X
                axisLine.quaternion.identity()
            }
            Axis.Y -> {
                val angleToCam = atan2(
                        avgPosition.x - view.editorCamera.position.x,
                        avgPosition.z - view.editorCamera.position.z
                )
                axisLine.quaternion.setRotation(0f, angleToCam, -PIf / 2f)
                axisLine.axis = Axis.Z
            }
            Axis.Z -> {
                axisLine.axis = Axis.X
                axisLine.quaternion.rotateXYZ(0f, -PIf / 2f, 0f)
            }
        }

        view.editorCamera.screenPointToRay(virtualMouse.x, virtualMouse.y, ray)
        collader.rayCast(ray, vec)
        startVec.set(vec)
    }

    override fun onStop() {
        super.onStop()
        axisLine.parent = null
        view.renderThread {
            axisLine.close()
        }
    }

    override fun render(dt: Float) {
        super.render(dt)

        val speedCof = if (slow) 0.1f else 1f
        moveDirection.add(
                (virtualMouse.x - old.x) * speedCof,
                (virtualMouse.y - old.y) * speedCof
        )
        old.set(virtualMouse)

        view.editorCamera.screenPointToRay(
                (moveDirection.x).toInt(),
                (moveDirection.y).toInt(),
                ray
        )
        collader.rayCast(ray, vec)
        vec.sub(startVec)
        val value = when (type) {
            Axis.X -> Vector3f(vec.x, 0f, 0f)
            Axis.Y -> Vector3f(0f, vec.y, 0f)
            Axis.Z -> Vector3f(0f, 0f, vec.z)
        }

        initPositions.forEach { (node, matrix) ->
            val m = Matrix4f()
            m.translate(value)
                    .mul(matrix)
            node.setGlobalTransform(m)
        }
        view.updatePropertyPosition()
    }
}
*/
@Strictfp
class EditMovement2D(view: SceneEditorView, selected: List<Spatial2D>, val type: Axis?) : AbstractEditMovement2D(view, selected) {
    override fun keyDown(code: Int) {
        when (code) {
            Keys.X -> {
                resetInitPosition()
                view.startEditor(EditMovement2D(view, selected, Axis.X))
            }
            Keys.Y -> {
                resetInitPosition()
                view.startEditor(EditMovement2D(view, selected, Axis.Y))
            }
            else -> super.keyDown(code)
        }
    }


    private val old = Vector2i(virtualMouse)
    private var moveDirection = Vector2f(0f, 0f)

    override fun render(dt: Float) {
        super.render(dt)

        val speedCof = if (slow) 0.5f else 1f
        moveDirection.add(
                (virtualMouse.x - old.x) * speedCof / view.editorCamera2D.zoom,
                (virtualMouse.y - old.y) * speedCof / view.editorCamera2D.zoom
        )
        old.set(virtualMouse)


        when (type) {
            Axis.X -> moveDirection.y = 0f
            Axis.Y -> moveDirection.x = 0f
        }
        engine.mathPool.vec3f.use1 { moveDirection3f ->
            moveDirection3f.set(moveDirection.x, moveDirection.y, 0f)
            initPositions.forEach { (node, nodeMatrix) ->
                engine.mathPool.mat4f.use1 { mat ->
                    mat.identity()
                            .translate(moveDirection3f)
                            .mul(nodeMatrix)
                    node.setGlobalTransform(mat)
                }
//                val mat = Matrix4f()
//                        .translate(moveDirection3f)
//                        .mul(nodeMatrix)
//                node.setGlobalTransform(mat)
            }
            updatePropertyPosition()
        }
    }

}