package pw.binom.sceneEditor

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBPanel
import pw.binom.FlexLayout
import pw.binom.appendTo
import pw.binom.sceneEditor.nodeController.NodeService
import java.awt.Component
import javax.swing.*
import javax.swing.event.ListDataListener

class NodeListRender : ListCellRenderer<NodeService.CreateItem> {
    private val label = JLabel().apply {
        isOpaque = true
    }

    override fun getListCellRendererComponent(list: JList<out NodeService.CreateItem>, value: NodeService.CreateItem, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component {

        if (isSelected) {
            label.background = list.selectionBackground
            label.foreground = list.selectionForeground
        } else {
            label.background = list.background
            label.foreground = list.foreground
        }
        label.text = value.name
        label.icon = value.icon
        return label
    }


}

class CreateNodeDialog(view: SceneEditorView, project: Project) : DialogWrapper(project, false, IdeModalityType.PROJECT) {
    private val root = JBPanel<JBPanel<*>>()
    private val layout = FlexLayout(root, FlexLayout.Direction.COLUMN)
    override fun createCenterPanel(): JComponent = root
    private val model = NodeListModel(view)
    private val list = JBList<NodeService.CreateItem>(model)

    var selected: NodeService.CreateItem? = null
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

private class NodeListModel(view: SceneEditorView) : ListModel<NodeService.CreateItem> {
    private val classes = view.services.flatMap { it.createItems }
    override fun getElementAt(index: Int): NodeService.CreateItem = classes[index]

    override fun getSize(): Int = classes.size

    override fun addListDataListener(l: ListDataListener?) {
    }

    override fun removeListDataListener(l: ListDataListener?) {
    }

}