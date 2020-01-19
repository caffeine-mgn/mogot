package pw.binom.sceneEditor.editors

import mogot.Spatial
import mogot.collider.Panel3DCollider
import mogot.math.*
import pw.binom.sceneEditor.Line
import pw.binom.sceneEditor.SceneEditorView
import pw.binom.sceneEditor.properties.Transform3DProperty
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent
import kotlin.math.*


interface EditActionFactory {
    fun mouseDown(view: SceneEditorView, e: MouseEvent) {}
    fun keyDown(view: SceneEditorView, e: KeyEvent) {}
}

object EditMoveFactory : EditActionFactory {
    override fun keyDown(view: SceneEditorView, e: KeyEvent) {
        if (e.keyCode == 71) {
            view.startEditor(EditMoveAllAxie(view, view.selected.mapNotNull { it as? Spatial }))
        }
    }
}

abstract class EditMove(view: SceneEditorView, selected: List<Spatial>) : SpatialEditor(view, selected) {

    protected open fun stopEdit() {
        view.stopEditing()
        view.lockMouse = false
        view.cursorVisible = true
    }

    init {
//        view.lockMouse = true
//        view.cursorVisible = false
    }
}

fun SceneEditorView.updatePropertyPosition() {
    editor1.propertyTool.properties
            .mapNotNull { it as? Transform3DProperty }
            .forEach {
                it.update()
            }
}

@Strictfp
class EditMoveOneAxis(view: SceneEditorView, selected: List<Spatial>, val type: Axis) : EditMove(view, selected) {

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
        /*
        val pos = Vector3f()
        val cof = if (slow) 0.1f else 1f

        selected.forEach {
            pos.set(it.position)
            it.parentSpatial?.localToGlobal(pos, pos)
            when (type) {
                Axis.X -> pos.x += (value - startValue) * cof
                Axis.Y -> pos.y += (startValue - value) * cof
                Axis.Z -> pos.z += (value - startValue) * cof
            }
            it.parentSpatial?.globalToLocal(pos, pos)
            it.position.set(pos)
        }
        */
        view.updatePropertyPosition()
        //startValue = value
    }
}
@Strictfp
class EditMoveAllAxie(view: SceneEditorView, selected: List<Spatial>) : EditMove(view, selected) {
    override fun keyDown(code: Int) {
        when (code) {
            Keys.X -> {
                resetInitPosition()
                view.startEditor(EditMoveOneAxis(view, selected, Axis.X))
            }
            Keys.Y -> {
                resetInitPosition()
                view.startEditor(EditMoveOneAxis(view, selected, Axis.Y))
            }
            Keys.Z -> {
                resetInitPosition()
                view.startEditor(EditMoveOneAxis(view, selected, Axis.Z))
            }
            else -> super.keyDown(code)
        }
    }


    private val old = Vector2i(virtualMouse)
    private var moveDirection = Vector2f(0f, 0f)

    override fun render(dt: Float) {
        super.render(dt)

        val speedCof = if (slow) 0.005f else 0.025f
        moveDirection.add(
                (virtualMouse.x - old.x) * speedCof,
                (old.y - virtualMouse.y) * speedCof
        )
        old.set(virtualMouse)


        val moveDirection3f = Vector3f(moveDirection.x, moveDirection.y, 0f)
        view.editorCamera.quaternion.mul(moveDirection3f, moveDirection3f)
        initPositions.forEach { t, u ->
            val mat = Matrix4f()
                    .translate(moveDirection3f)
                    .mul(u)
            t.setGlobalTransform(mat)
        }
        view.updatePropertyPosition()
    }

}