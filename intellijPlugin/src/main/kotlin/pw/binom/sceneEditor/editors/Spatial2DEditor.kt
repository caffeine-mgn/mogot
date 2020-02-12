package pw.binom.sceneEditor.editors

import mogot.Spatial2D
import mogot.math.*
import pw.binom.sceneEditor.SceneEditorView
import pw.binom.sceneEditor.properties.Transform2DProperty
import java.awt.event.MouseEvent

abstract class Spatial2DEditor(view: SceneEditorView, val selected: List<Spatial2D>) : EditorWithVirtualMouse(view) {
    val initPositions = selected.asSequence().map {
        it to it.localToGlobalMatrix(Matrix4f())
    }.toMap()
    val avgPosition: Vector2f// = selected.asSequence().map { it.position }.avg()

    init {
        val v = view.engine.mathPool.vec2f.poll()
        val avr = Vector2f()
        selected.asSequence().forEach {
            v.set(0f, 0f)
            it.localToGlobal(v, v)
            avr.add(v)
        }
        avr.set(avr.x / selected.size.toFloat(), avr.y / selected.size.toFloat())
        view.engine.mathPool.vec2f.push(v)
        avgPosition = avr
    }

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