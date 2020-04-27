package pw.binom.ui

import mogot.math.Vector2f
import mogot.math.*
import pw.binom.sceneEditor.NodeService
import pw.binom.sceneEditor.SceneEditor
import pw.binom.sceneEditor.properties.Panel
import pw.binom.utils.common
import java.awt.Cursor
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.dnd.*
import java.io.Closeable

class EditorVec2(sceneEditor: SceneEditor, fields: List<NodeService.Field>) : AbstractEditor(sceneEditor, fields) {

    private val layout = gridBagLayout()
    private val title = PropertyName(fields.first().displayName).appendTo(layout, 0, 0)
    private val editor = Vector2Value().appendTo(layout, 1, 0)

    private var enableEvents = true
    private var enableEvents2 = true

    private fun refreshValues() {
        enableEvents = false
        editor.value.set(fields.asSequence().map { it.currentValue as Vector2fc }.common)
        enableEvents = true
    }

    init {
        fields.forEach {
            closable += it.eventChange.on {
                if (enableEvents2)
                    refreshValues()
            }
            Unit
        }
        editor.eventChange.on {
            enableEvents2 = false
            val x = editor.value.x
            val y = editor.value.y
            if (enableEvents)
                when {
                    !x.isNaN() && !y.isNaN() -> {
                        val v = Vector2f(x, y)
                        fields.forEach {
                            it.clearTempValue()
                            it.currentValue = v
                        }
                    }
                    !editor.value.x.isNaN() ->

                        fields.forEach {
                            val currentValue = it.currentValue as Vector2fc
                            it.clearTempValue()
                            it.currentValue = Vector2f(x, currentValue.y)
                        }
                    !editor.value.y.isNaN() ->
                        fields.forEach {
                            val currentValue = it.currentValue as Vector2fc
                            it.clearTempValue()
                            it.currentValue = Vector2f(currentValue.x, y)
                        }

                }
            enableEvents2 = true
        }
        refreshValues()
    }
}