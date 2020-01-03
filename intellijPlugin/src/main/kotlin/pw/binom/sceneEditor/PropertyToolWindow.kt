package pw.binom.sceneEditor

import com.intellij.ui.components.JBScrollPane
import mogot.Node
import pw.binom.FlexLayout
import pw.binom.appendTo
import pw.binom.sceneEditor.properties.Panel
import pw.binom.sceneEditor.properties.Property

class PropertyToolWindow(val editor: SceneEditor) : JBScrollPane() {
    private val panel = Panel()
    private val layout = FlexLayout(panel, FlexLayout.Direction.COLUMN)
    private val _properties = ArrayList<Property>()
    val properties: List<Property>
        get() = _properties

    init {
        this.setViewportView(panel)
    }

    fun setNodes(nodes: List<Node>) {
        val nodes = nodes.asSequence()
                .map { it to editor.viewer.getService(it) }
                .filter {
                    it.second != null
                }
                .map {
                    it.first to it.second!!
                }.toList()

        _properties.forEach {
            layout.remove(it.component)
        }
        _properties.clear()
        nodes.asSequence()
                .flatMap { it.second.getProperties(editor.viewer, it.first).asSequence() }
                .distinct()
                .filter { property ->
                    nodes.all { property in it.second.getProperties(editor.viewer, it.first) }
                }
                .map { editor.getProperty(it) }
                .forEach {
                    _properties += it
                    it.component.appendTo(layout) {
                        grow = 0f
                    }
                }

        _properties.forEach {
            it.setNodes(nodes.map { it.first })
        }

//        repaint()
        panel.repaint()
    }
}