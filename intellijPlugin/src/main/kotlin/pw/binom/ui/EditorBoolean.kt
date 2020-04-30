package pw.binom.ui

import pw.binom.sceneEditor.NodeService
import pw.binom.sceneEditor.SceneEditor
import pw.binom.utils.common
import javax.swing.JCheckBox

class EditorBoolean(sceneEditor: SceneEditor, fields: List<NodeService.Field>) : AbstractEditor(sceneEditor, fields) {

    private val layout = gridBagLayout()
    private val title = PropertyName(fields.first().displayName).appendTo(layout, 0, 0)
    private val editor = JCheckBox().appendTo(layout, 1, 0)

    private var enableEvents = true
    private var enableEvents2 = true

    private fun refreshValues() {
        enableEvents = false
        editor.isSelected = fields.asSequence().map { it.currentValue as Boolean }.common ?: false
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
        editor.addActionListener {
            enableEvents2 = false
            val value = editor.isSelected
            if (enableEvents) {
                fields.forEach {
                    it.clearTempValue()
                    it.currentValue = value
                }
            }
            enableEvents2 = true
        }
        refreshValues()
    }
}