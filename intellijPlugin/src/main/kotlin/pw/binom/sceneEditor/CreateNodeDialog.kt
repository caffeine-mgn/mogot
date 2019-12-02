package pw.binom.sceneEditor

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBPanel
import mogot.OmniLight
import mogot.annotation.ComponentNode
import pw.binom.FlexLayout
import pw.binom.appendTo
import java.awt.Component
import javax.swing.*
import javax.swing.event.ListDataListener

class NodeListRender : ListCellRenderer<Class<*>> {
    private val label = JLabel().apply {
        isOpaque = true
    }
    private val lightIcon = ImageIcon(this::class.java.classLoader.getResource("/light-icon-16.png"))

    override fun getListCellRendererComponent(list: JList<out Class<*>>, value: Class<*>, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component {

        if (isSelected) {
            label.background = list.selectionBackground
            label.foreground = list.selectionForeground
        } else {
            label.background = list.background
            label.foreground = list.foreground
        }

        val componentNode = value.getAnnotation(ComponentNode::class.java)
        label.text = componentNode?.displayName?.takeIf { it.isNotBlank() } ?: value.simpleName
        label.icon = when (value) {
            OmniLight::class.java -> lightIcon
            else -> null
        }
        return label
    }


}

class CreateNodeDialog(project: Project) : DialogWrapper(project, false, IdeModalityType.PROJECT) {
    private val root = JBPanel<JBPanel<*>>()
    private val layout = FlexLayout(root, FlexLayout.Direction.COLUMN)
    override fun createCenterPanel(): JComponent = root
    private val model = NodeListModel()
    private val list = JBList<Class<*>>(model)

    var selected: Class<*>? = null
        private set

    init {
        init()
        title = "Select Type of New Node"
        list.cellRenderer = NodeListRender()
        list.appendTo(layout) {
            basis = 140
        }

        list.addListSelectionListener {
            refreshOkBtn()
            val index = list.selectedIndex
            selected = if (index >= 0) model.getElementAt(index) else null
        }

        setSize(300, 300)

        refreshOkBtn()
    }

    private fun refreshOkBtn() {
        isOKActionEnabled = list.selectedIndex >= 0
    }

}

private class NodeListModel : ListModel<Class<*>> {
    private val classes = listOf<Class<*>>(OmniLight::class.java)
    override fun getElementAt(index: Int): Class<*> = classes[index]

    override fun getSize(): Int = classes.size

    override fun addListDataListener(l: ListDataListener?) {
    }

    override fun removeListDataListener(l: ListDataListener?) {
    }

}