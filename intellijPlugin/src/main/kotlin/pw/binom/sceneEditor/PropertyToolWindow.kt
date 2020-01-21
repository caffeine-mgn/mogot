package pw.binom.sceneEditor

import com.intellij.ui.components.JBScrollPane
import mogot.Node
import pw.binom.FlexLayout
import pw.binom.appendTo
import pw.binom.sceneEditor.properties.Panel
import pw.binom.sceneEditor.properties.Property
import pw.binom.ui.StringValue

class PropertyToolWindow(val editor: SceneEditor) : JBScrollPane() {
    private val panel = Panel()
    private val layout = FlexLayout(panel, FlexLayout.Direction.COLUMN)
    private val _properties = ArrayList<Property>()
    val idEditor = StringValue("ID ").appendTo(layout, grow = 0)
    val properties: List<Property>
        get() = _properties
    private var nodes: List<Node>? = null

    init {
        this.setViewportView(panel)
        idEditor.eventChange.on(this::onIdChange)
    }

    private var enabkeChangeDispatcher = false
    private fun onIdChange() {
        if (!enabkeChangeDispatcher)
            return
        nodes?.takeIf { it.size == 1 }?.first()?.id = idEditor.value.takeIf { it.isNotBlank() }
    }

    fun setNodes(nodes: List<Node>) {
        this.nodes = nodes
        idEditor.isEnabled = nodes.size == 1
        if (idEditor.isEnabled) {
            enabkeChangeDispatcher = false
            idEditor.value = nodes.first().id ?: ""
            enabkeChangeDispatcher = true
        }
        val nodes = nodes.asSequence()
                .map { it to editor.viewer.view.getService(it) }
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
                .flatMap { it.second.getProperties(editor.viewer.view, it.first).asSequence() }
                .distinct()
                .filter { property ->
                    nodes.all { property in it.second.getProperties(editor.viewer.view, it.first) }
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