package pw.binom.sceneEditor

import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.treeStructure.Tree
import mogot.Node
import mogot.OmniLight
import mogot.annotation.ComponentNode
import mogot.asUpSequence
import pw.binom.FlexLayout
import pw.binom.appendTo
import pw.binom.async
import java.awt.Color
import java.awt.Component
import javax.swing.ImageIcon
import javax.swing.JLabel
import javax.swing.JTree
import javax.swing.event.TreeModelEvent
import javax.swing.event.TreeModelListener
import javax.swing.tree.TreeCellRenderer
import javax.swing.tree.TreeModel
import javax.swing.tree.TreePath

private class NodeCellRender : TreeCellRenderer {
    val label = JLabel()
    val lightIcon = ImageIcon(this::class.java.classLoader.getResource("/light-icon-16.png"))
    override fun getTreeCellRendererComponent(tree: JTree?, value: Any, selected: Boolean, expanded: Boolean, leaf: Boolean, row: Int, hasFocus: Boolean): Component {
        value as Node
        val componentNode = value::class.java.getAnnotation(ComponentNode::class.java)
        label.text = value.id ?: componentNode?.displayName?.takeIf { it.isNotBlank() } ?: value::class.java.simpleName
        label.icon = when (value) {
            is OmniLight -> lightIcon
            else -> null
        }
        return label
    }
}

class SceneStruct(val view: SceneEditorView) : JBPanel<JBPanel<*>>() {
    private val model = SceneTreeModel(view)
    val tree = Tree(model)
    val scrollPane = JBScrollPane(tree)
    private val layout = FlexLayout(this, FlexLayout.Direction.COLUMN)

    init {

        val pp = ToolbarDecorator.createDecorator(tree)
                .setAddAction {
                    val parent = tree.lastSelectedPathComponent as Node? ?: view.sceneRoot

                    val d = CreateNodeDialog(view.project)
                    if (d.showAndGet()) {
                        when (d.selected) {
                            OmniLight::class.java -> {
                                async {
                                    val light = view.addOmniLight(parent)
                                    model.created(tree, light)
                                }
                            }
                        }
                    }
                }
                .setRemoveAction {
                    val node = tree.lastSelectedPathComponent as Node?
                    node ?: return@setRemoveAction
                    model.delete(tree, node)
                    node.parent = null
                    node.close()
                }
                .createPanel()

        tree.cellRenderer = NodeCellRender()
        tree.showsRootHandles = true
        pp.appendTo(layout) {
            grow = 0f
        }
        scrollPane.appendTo(layout) {
            grow = 1f
        }
        scrollPane.background = Color.RED
        tree.addTreeSelectionListener {
            view.select(it.path.lastPathComponent as Node?)

        }
    }
}

private class SceneTreeModel(val view: SceneEditorView) : TreeModel {
    override fun getRoot(): Any = view.sceneRoot

    override fun isLeaf(node: Any?): Boolean = (node as Node?)?.childs?.isEmpty() ?: false

    override fun getChildCount(parent: Any?): Int = (parent as Node?)?.childs?.size ?: 0

    override fun removeTreeModelListener(l: TreeModelListener?) {
        l ?: return
        listeners -= l
    }

    override fun valueForPathChanged(path: TreePath?, newValue: Any?) {
    }

    override fun getIndexOfChild(parent: Any?, child: Any?): Int {
        (parent as Node?) ?: return -1
        return parent?.childs?.indexOf(child) ?: -1
    }

    fun created(source: Tree, node: Node) {
        val path = (node.asUpSequence().filter { it !== view.editorRoot }.toList()).reversed()
        val index = node.parent!!.childs.indexOf(node)
        val e = TreeModelEvent(source, path.toTypedArray(), intArrayOf(index), Array(1) { node })
        listeners.forEach {
            it.treeNodesInserted(e)
        }
    }

    fun delete(source: Tree, node: Node) {
        val path = (node.asUpSequence().filter { it !== view.editorRoot }.toList()).reversed()
        val index = node.parent!!.childs.indexOf(node)
        val e = TreeModelEvent(source, path.toTypedArray(), intArrayOf(index), Array(1) { node })
        listeners.forEach {
            it.treeNodesRemoved(e)
        }
    }

    override fun getChild(parent: Any?, index: Int): Any? = (parent as Node?)?.childs?.get(index)
    private val listeners = ArrayList<TreeModelListener>()
    override fun addTreeModelListener(l: TreeModelListener?) {
        l ?: return
        listeners += l
    }
}