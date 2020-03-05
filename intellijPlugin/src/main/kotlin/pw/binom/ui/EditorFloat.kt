package pw.binom.ui

import mogot.math.Vector2f
import mogot.math.Vector2fc
import mogot.math.set
import pw.binom.sceneEditor.NodeService
import pw.binom.sceneEditor.SceneEditor
import pw.binom.utils.common

class EditorFloat(sceneEditor: SceneEditor, fields: List<NodeService.FieldFloat>) : AbstractEditor<Float>(sceneEditor, fields) {

    private val layout = gridBagLayout()
    private val title = PropertyName(fields.first().displayName).appendTo(layout, 0, 0)
    private val editor = FloatEditText().appendTo(layout, 1, 0)

    private var enableEvents = true
    private var enableEvents2 = true

    private fun refreshValues() {
        enableEvents = false
        editor.value = fields.asSequence().map { it.currentValue }.common
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
                when {
                    !value.isNaN() -> {
                        fields.forEach {
                            it.clearTempValue()
                            it.currentValue = value
                        }
                    }
                }
            enableEvents2 = true
        }
        refreshValues()
    }
}