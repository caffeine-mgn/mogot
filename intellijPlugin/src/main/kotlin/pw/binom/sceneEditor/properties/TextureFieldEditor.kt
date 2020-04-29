package pw.binom.sceneEditor.properties

import pw.binom.sceneEditor.NodeService
import pw.binom.sceneEditor.SceneEditor
import pw.binom.ui.*
import pw.binom.utils.common

class TextureFieldEditor(sceneEditor: SceneEditor, fields: List<NodeService.Field>) : AbstractEditor(sceneEditor, fields) {
    private val layout = gridBagLayout()
    private val title = PropertyName(fields.first().displayName).appendTo(layout, 0, 0)
    private val editor = TextureSelector(sceneEditor).appendTo(layout, 1, 0)

    private var enableEvents = true
    private var enableEvents2 = true

    private fun refreshValues() {
        enableEvents = false
        editor.value = fields.asSequence().map { it.currentValue as String }.common ?: ""
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
            val value = editor.value
            if (enableEvents)
                fields.forEach {
                    it.clearTempValue()
                    it.currentValue = value
                }
            enableEvents2 = true
        }
        refreshValues()
    }
}