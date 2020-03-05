package pw.binom.sceneEditor.properties

import pw.binom.FlexLayout
import pw.binom.appendTo
import pw.binom.sceneEditor.NodeService
import pw.binom.sceneEditor.SceneEditor
import pw.binom.ui.EditorFloat
import pw.binom.ui.EditorVec2
import pw.binom.ui.EditorVec3
import java.io.Closeable

class PropertyGroupSpoler(val sceneEditor: SceneEditor, title: String, list: List<NodeService.Field<*>>) : Spoler(title), Closeable {
    private val layout = FlexLayout(stage, FlexLayout.Direction.COLUMN)
    private val closable = ArrayList<Closeable>()

    init {
        list.groupBy { it.id }.values.forEach {
            when (it.first()) {
                is NodeService.FieldVec2 -> closable += EditorVec2(sceneEditor, it as List<NodeService.FieldVec2>).appendTo(layout, grow = 0)
                is NodeService.FieldFloat -> closable += EditorFloat(sceneEditor, it as List<NodeService.FieldFloat>).appendTo(layout, grow = 0)
                is NodeService.FieldFloat -> closable += EditorVec3(sceneEditor, it as List<NodeService.FieldVec3>).appendTo(layout, grow = 0)
            }
        }
    }

    override fun close() {
        closable.forEach {
            it.close()
        }
    }
}