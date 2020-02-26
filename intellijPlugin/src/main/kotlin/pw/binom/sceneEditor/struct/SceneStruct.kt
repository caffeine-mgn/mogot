package pw.binom.sceneEditor.struct

import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBPanel
import com.intellij.ui.treeStructure.Tree
import mogot.Node
import mogot.waitFrame
import pw.binom.sceneEditor.CreateNodeDialog
import pw.binom.sceneEditor.SceneEditorView
import javax.swing.DropMode
import javax.swing.SpringLayout

class SceneStruct(val view: SceneEditorView) : JBPanel<JBPanel<*>>() {
    val tree = Tree()
    val model = SceneTreeModel(view)


    private val _layout = SpringLayout()

    init {
        tree.model = model
        tree.dragEnabled = true
        tree.dropMode = DropMode.ON_OR_INSERT
        tree.transferHandler = TreeTransferHandler()
        tree.expandsSelectedPaths = true
        this.layout = _layout
        val pp = ToolbarDecorator.createDecorator(tree)
                .setAddAction {
                    val parent = tree.lastSelectedPathComponent as Node? ?: view.sceneRoot

                    val d = CreateNodeDialog(view, view.project)
                    if (d.showAndGet()) {
                        view.renderThread {
                            val creator = d.selected ?: return@renderThread
                            val node = creator.create(view) ?: return@renderThread
                            node.parent = parent
                            model.created(tree, node)
                            view.mode = view.mode
                        }
                    }
                }
                .setRemoveAction {
                    val nodes = tree.selectionPaths
                            ?.map { it.lastPathComponent as Node }
                            ?.filter { it.parent != null }
                            ?.takeIf { it.isNotEmpty() }
                    nodes ?: return@setRemoveAction
                    nodes.forEach {
                        model.delete(tree, it)
                        view.removeNode(it)
                    }
                    /*
                    nodes.reversed().forEach {
                        model.delete(tree, it)
                        it.parent = null
                    }
                    view.engine.waitFrame {
                        nodes.forEach {
                            it.close()
                        }
                    }
                    */
                }
                .createPanel()

        tree.cellRenderer = NodeCellRender()
        tree.showsRootHandles = true
        add(pp)
        _layout.putConstraint(SpringLayout.NORTH, pp, 0, SpringLayout.NORTH, this)
        _layout.putConstraint(SpringLayout.EAST, pp, 10, SpringLayout.EAST, this)
        _layout.putConstraint(SpringLayout.WEST, pp, 0, SpringLayout.WEST, this)
        _layout.putConstraint(SpringLayout.SOUTH, pp, 0, SpringLayout.SOUTH, this)

        tree.addTreeSelectionListener {
            view.select(tree.selectionModel.selectionPaths.map { it.lastPathComponent as Node })
        }
    }
}