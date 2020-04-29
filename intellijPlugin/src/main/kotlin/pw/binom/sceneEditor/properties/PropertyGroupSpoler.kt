package pw.binom.sceneEditor.properties

import pw.binom.FlexLayout
import pw.binom.appendTo
import pw.binom.sceneEditor.NodeService
import pw.binom.sceneEditor.SceneEditor
import pw.binom.ui.AbstractEditor
import java.io.Closeable
import javax.swing.JComponent
import javax.swing.JPanel

interface Properties : Closeable {
    val component: JComponent
}

private class FieldControl(val sceneEditor: SceneEditor, val layout: FlexLayout, val list: List<NodeService.Field>) : Closeable {
    private val closable = ArrayList<Closeable>()
    private val closable2 = ArrayList<pw.binom.io.Closeable>()
    private val subPanels = HashMap<NodeService.Field, ArrayList<Properties>>()
    private val editors = HashMap<NodeService.Field, AbstractEditor>()

    private fun updateSubFields(field: NodeService.Field) {
        val editor = editors[field]!!
        subPanels[field]?.forEach {
            layout.componentPlace.remove(it.component)
        }
        subPanels[field]?.clear()

        list.asSequence()
                .filter { it.id == field.id }
                .flatMap { it.getSubFields().asSequence() }
                .groupBy { it.groupName }
                .forEach {
                    val p:Properties = if (it.key.isEmpty()) {
                        PropertyGroup(sceneEditor, it.value)
                    } else {
                        PropertyGroupSpoler(sceneEditor, it.key, it.value)
                    }
                    p.component.appendTo(layout, grow = 0, after = editor)
                    subPanels.getOrPut(field) { ArrayList() }.add(p)
                }
    }

    init {
        list.groupBy { it.id }.values.forEach {
            val field = it.first()
            val editor = field.makeEditor(sceneEditor, it).appendTo(layout, grow = 0)
            editors[field] = editor
            closable += editor
            val subFields = it.first().getSubFields()
            closable2 += field.subFieldsEventChange.on {
                updateSubFields(field)
            }

            updateSubFields(field)
        }
    }

    override fun close() {
        closable.forEach {
            it.close()
        }
        closable2.forEach {
            it.close()
        }
    }
}

class PropertyGroup(val sceneEditor: SceneEditor, list: List<NodeService.Field>) : JPanel(), Closeable, Properties {
    private val layout = FlexLayout(this, FlexLayout.Direction.COLUMN)
    private val fc = FieldControl(sceneEditor, layout, list)
    override val component: JComponent
        get() = this

    override fun close() {
        fc.close()
    }
}

class PropertyGroupSpoler(val sceneEditor: SceneEditor, title: String, list: List<NodeService.Field>) : Spoler(title), Closeable, Properties {
    private val layout = FlexLayout(stage, FlexLayout.Direction.COLUMN)
    private val fc = FieldControl(sceneEditor, layout, list)
    override val component: JComponent
        get() = this


    override fun close() {
        fc.close()
    }
}