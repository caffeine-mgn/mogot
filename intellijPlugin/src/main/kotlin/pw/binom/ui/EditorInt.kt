package pw.binom.ui

import mogot.math.Vector2f
import mogot.math.Vector2fc
import mogot.math.set
import pw.binom.sceneEditor.NodeService
import pw.binom.sceneEditor.SceneEditor
import pw.binom.utils.common

class EditorInt(sceneEditor: SceneEditor, fields: List<NodeService.Field<Int>>) : AbstractEditor<Int>(sceneEditor, fields) {

    private val layout = gridBagLayout()
    private val title = PropertyName(fields.first().displayName).appendTo(layout, 0, 0)
    private val editor = IntEditText().appendTo(layout, 1, 0)

    private var enableEvents = true
    private var enableEvents2 = true

    private fun refreshValues() {
        enableEvents = false
        val common = fields.asSequence().map { it.currentValue }.common
        editor.value = common ?: 0
        editor.valid = common != null
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
            val value = if (editor.valid) editor.value else null
            if (value != null && enableEvents)
                fields.forEach {
                    it.clearTempValue()
                    it.currentValue = value
                }
            enableEvents2 = true
        }
        refreshValues()
    }
}