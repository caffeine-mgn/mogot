package pw.binom.sceneEditor

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBPanel
import pw.binom.FlexLayout
import pw.binom.Services
import pw.binom.appendTo
import java.awt.Component
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.swing.*
import javax.swing.event.ListDataListener

class NodeListRender : ListCellRenderer<NodeCreator> {
    private val label = JLabel().apply {
        isOpaque = true
    }

    override fun getListCellRendererComponent(list: JList<out NodeCreator>, value: NodeCreator, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component {

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
    private val list = JBList<NodeCreator>(model)

    var selected: NodeCreator? = null
        private set

    init {
        init()
        title = "Select Type of New Node"
        list.cellRenderer = NodeListRender()
        list.appendTo(layout)

        list.addListSelectionListener {
            refreshOkBtn()
            val index = list.selectedIndex
            selected = if (index >= 0) model.getElementAt(index) else null
        }

        list.addMouseListener(object : MouseListener {
            override fun mouseReleased(e: MouseEvent?) {
            }

            override fun mouseEntered(e: MouseEvent?) {
            }

            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount >= 2) {
                    close(OK_EXIT_CODE, true)
                }
            }

            override fun mouseExited(e: MouseEvent?) {
            }

            override fun mousePressed(e: MouseEvent?) {
            }

        })
        setSize(300, 300)

        refreshOkBtn()
    }

    private fun refreshOkBtn() {
        isOKActionEnabled = list.selectedIndex >= 0
    }

}

private class NodeListModel(view: SceneEditorView) : ListModel<NodeCreator> {
    private val creators by Services.byClassSequence(NodeCreator::class.java)
    private val classes = creators.toList()

    override fun getElementAt(index: Int): NodeCreator = classes[index]

    override fun getSize(): Int = classes.size

    override fun addListDataListener(l: ListDataListener?) {
    }

    override fun removeListDataListener(l: ListDataListener?) {
    }

}