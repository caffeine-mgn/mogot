package pw.binom.sceneEditor.editors

import mogot.Spatial2D
import mogot.math.Matrix4f
import pw.binom.sceneEditor.SceneEditorView
import pw.binom.sceneEditor.avg
import pw.binom.sceneEditor.properties.Transform2DProperty
import java.awt.event.MouseEvent

abstract class Spatial2DEditor(view: SceneEditorView, val selected: List<Spatial2D>) : EditorWithVirtualMouse(view) {
    val initPositions = selected.asSequence().map {
        it to it.localToGlobalMatrix(Matrix4f())
    }.toMap()
    val avgPosition = selected.asSequence().map { it.position }.avg()

    override fun resetInitPosition() {
        initPositions.forEach { (node, matrix) ->
            node.setGlobalTransform(matrix)
        }
        updatePropertyPosition()
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

    fun updatePropertyPosition() {
        view.editor1.propertyTool.properties
                .mapNotNull { it as? Transform2DProperty }
                .forEach {
                    it.update()
                }
    }
}