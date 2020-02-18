package pw.binom.sceneEditor.struct

import com.intellij.ui.treeStructure.Tree
import mogot.Node
import mogot.asUpSequence
import pw.binom.sceneEditor.SceneEditorView
import javax.swing.JTree
import javax.swing.event.TreeModelEvent
import javax.swing.event.TreeModelListener
import javax.swing.tree.TreeModel
import javax.swing.tree.TreePath

class SceneTreeModel(val view: SceneEditorView) : TreeModel {
    override fun getRoot(): Any = view.sceneRoot

    override fun isLeaf(node: Any?): Boolean {
        node as Node
        if (view.getService(node)?.isInternalChilds(node) == true)
            return true
        return node.childs.isEmpty()
    }

    override fun getChildCount(parent: Any?): Int {
        parent as Node
        if (view.getService(parent)?.isInternalChilds(parent) == true)
            return 0
        return parent.childs.size
    }

    override fun removeTreeModelListener(l: TreeModelListener?) {
        l ?: return
        listeners -= l
    }

    fun move(source: JTree, node: Node, oldIndex: Int, oldParent: Node, newIndex: Int, newParent: Node) {

//        val remove = TreeModelEvent(source, oldParent.makeTreePath())
//        val add = TreeModelEvent(source, newParent.makeTreePath())
        val remove = TreeModelEvent(source, oldParent.makeTreePath(), intArrayOf(oldIndex), Array(1) { node })
        val add = TreeModelEvent(source, newParent.makeTreePath(), intArrayOf(newIndex), Array(1) { node })
        println("MOVE $oldIndex -> $newIndex   ${newParent.childs.indexOf(node)}")
        listeners.forEach {
            it.treeStructureChanged(remove)
            it.treeStructureChanged(add)
        }
        /*
        val remove = TreeModelEvent(source, oldParent.makeTreePath(), intArrayOf(oldIndex), Array(1) { node })
        val add = TreeModelEvent(source, newParent.makeTreePath(), intArrayOf(newIndex), Array(1) { node })
        listeners.forEach {
            it.treeNodesRemoved(remove)
        }

        listeners.forEach {
            it.treeNodesInserted(add)
        }
         */
    }

    override fun valueForPathChanged(path: TreePath, newValue: Any) {
        throw RuntimeException("Not supported")
    }

    override fun getIndexOfChild(parent: Any?, child: Any?): Int {
        (parent as Node?) ?: return -1
        return parent?.childs?.indexOf(child) ?: -1
    }

    fun created(source: Tree, node: Node) {
        val path = (node.asUpSequence().filter { it !== view.editorRoot }.toList()).reversed()
        val index = node.parent!!.childs.indexOf(node)
        val e = TreeModelEvent(source, path.toTypedArray(), intArrayOf(index), Array(1) { node })
        listeners.toTypedArray().forEach {
            it.treeNodesInserted(e)
        }
    }

    fun delete(source: Tree, node: Node) {
        val path = (node.asUpSequence().filter { it !== view.editorRoot }.toList()).reversed()
        val index = node.parent!!.childs.indexOf(node)
        val e = TreeModelEvent(source, path.toTypedArray(), intArrayOf(index), Array(1) { node })
        listeners.toTypedArray().forEach {
            it.treeNodesRemoved(e)
        }
    }

    override fun getChild(parent: Any?, index: Int): Any? = (parent as Node?)?.childs?.getOrNull(index)
    private val listeners = ArrayList<TreeModelListener>()
    override fun addTreeModelListener(l: TreeModelListener?) {
        l ?: return
        listeners += l
    }
}