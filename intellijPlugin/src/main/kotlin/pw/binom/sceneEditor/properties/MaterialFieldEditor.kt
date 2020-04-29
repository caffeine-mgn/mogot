package pw.binom.sceneEditor.properties

import pw.binom.sceneEditor.NodeService
import pw.binom.sceneEditor.SceneEditor
import pw.binom.ui.*
import pw.binom.utils.common

class MaterialFieldEditor(sceneEditor: SceneEditor, fields: List<NodeService.Field>) : AbstractEditor(sceneEditor, fields) {
    private val layout = gridBagLayout()
    private val title = PropertyName(fields.first().displayName).appendTo(layout, 0, 0)
    private val editor = MaterialEditor(sceneEditor).appendTo(layout, 1, 0)

    private var enableEvents = true
    private var enableEvents2 = true

    private fun refreshValues() {
        val common = fields.asSequence().map { it.currentValue as String }.common ?: ""
        println("Material path: $common [${fields.map { it.displayName }}]")
        enableEvents = false
        editor.value = common
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