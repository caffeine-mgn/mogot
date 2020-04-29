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

class EditorVec3(sceneEditor: SceneEditor, fields: List<NodeService.Field>) : AbstractEditor(sceneEditor, fields) {

    private val layout = gridBagLayout()
    private val title = PropertyName(fields.first().displayName).appendTo(layout, 0, 0)
    private val editor = Vector3Value().appendTo(layout, 1, 0)

    private var enableEvents = true
    private var enableEvents2 = true

    private fun refreshValues() {
        enableEvents = false
        editor.value.set(fields.asSequence().map { it.currentValue as Vector3fc }.common)
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
            val z = editor.value.z
            if (enableEvents && (!x.isNaN() || !y.isNaN() || !z.isNaN())) {

                if (!x.isNaN() && !y.isNaN() && !z.isNaN()) {
                    val v = Vector3f(x, y, z)
                    fields.forEach {
                        it.clearTempValue()
                        it.currentValue = v
                    }

                } else {
                    fields.forEach {
                        val currentValue = it.currentValue as Vector3fc
                        it.currentValue = Vector3f(
                                x.takeIf { !it.isNaN() } ?: currentValue.x,
                                y.takeIf { !it.isNaN() } ?: currentValue.y,
                                z.takeIf { !it.isNaN() } ?: currentValue.z
                        )
                    }
                }
            }
            enableEvents2 = true
        }
        refreshValues()
    }
}