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
    private val properties = ArrayList<Property>()

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

        properties.forEach {
            layout.remove(it.component)
        }
        properties.clear()
        nodes.asSequence()
                .flatMap { it.second.getProperties(editor.viewer, it.first).asSequence() }
                .distinct()
                .filter { property ->
                    nodes.all { property in it.second.getProperties(editor.viewer, it.first) }
                }
                .map { editor.getProperty(it) }
                .forEach {
                    properties += it
                    it.component.appendTo(layout) {
                        grow = 0f
                    }
                }

        properties.forEach {
            it.setNodes(nodes.map { it.first })
        }

//        repaint()
        panel.repaint()
    }
}