package pw.binom.sceneEditor.struct

import mogot.*
import mogot.math.*
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import javax.swing.JComponent
import javax.swing.JTree
import javax.swing.TransferHandler
import javax.swing.tree.TreePath

class TreeTransferHandler : TransferHandler() {
    companion object {
        val mimeType = "x-mogot/node"

        val nodesFlavor = DataFlavor(mimeType)
        val flavors = arrayOf(nodesFlavor)
    }

    val nodesToRemove = Array<Node>(0) { TODO() }


    override fun canImport(support: TransferHandler.TransferSupport): Boolean {
        if (!support.isDrop) {
            println("support.isDrop == false")
            return false;
        }
        val data = support.transferable.getTransferData(nodesFlavor)
        val moveNodes = (data as? List<Node>)
        if (moveNodes == null) {
            println("moveNodes is null  support.transferable=${support.transferable::class.java.name}")
            return false
        }
        support.setShowDropLocation(true);
        if (!support.isDataFlavorSupported(nodesFlavor)) {
            println("support.isDataFlavorSupported == false")
            return false
        }
        // Do not allow a drop on the drag source selections.
        val dl = support.dropLocation as JTree.DropLocation
        val dropTarget = dl.path.lastPathComponent as Node?
        //println("dropTarget=${dropTarget}")
        return true
    }

    override fun exportDone(source: JComponent?, data: Transferable?, action: Int) {
        println("exportDone!")
        super.exportDone(source, data, action)
    }

    override fun createTransferable(c: JComponent): Transferable? {
        val tree = c as JTree
        val paths = tree.selectionPaths
        if (paths != null) {
            val nodes = paths.map { it.lastPathComponent as Node }
            val nodeForMove = nodes.filter { node ->
                !nodes.any { it.isChild(node) } && node.parent != null
            }
            println("Node For Move:")
            nodeForMove.forEach {
                println("${it::class.java.name} - ${it.id}")
            }
            if (nodeForMove.isEmpty())
                return null
            return NodesTransferable(nodeForMove)
        }
        return null
    }

    override fun getSourceActions(c: JComponent): Int {
        return COPY_OR_MOVE;
    }


    override fun importData(support: TransferSupport): Boolean {
        try {
            println("IMport Data!")
            if (!canImport(support)) {
                println("Can't import")
                return false
            }
            // Extract transfer data.
            val t = support.getTransferable();
            var nodes = t.getTransferData(nodesFlavor) as? List<Node> ?: return false
            val dl = support.dropLocation as JTree.DropLocation
            println("IMPORT into ${(dl.path.lastPathComponent as Node).id} as index: ${dl.childIndex}")
            // Get drop location info.

            val childIndex = dl.childIndex;
            val dest = dl.path;
            val parent =
                    dest.getLastPathComponent() as Node
            val tree = support.getComponent() as JTree
            val model = tree.getModel() as SceneTreeModel
            // Configure for drop mode.
            var index = childIndex;    // DropMode.INSERT
            if (childIndex == -1) {     // DropMode.ON
                index = parent.childs.size
            }
            if (support.isDrop) {
                println("Drag")
            } else {
                println("Past")
            }

            val targetNode = dl.path.lastPathComponent as Node
            nodes = nodes.filter { it !== targetNode }

            class Move(val node: Node, val oldParent: Node, val oldIndex: Int, val newParent: Node, var newIndex: Int)

            val pathes = ArrayList<Move>(nodes.size)
            val mat = Matrix4f()
            nodes.forEach {
                val oldParent = it.parent!!
                val oldIndex = oldParent.childs.indexOf(it)
                if (it is Spatial) {
                    it.localToGlobalMatrix(mat)
                }
                if (it is Spatial2D) {
                    it.localToGlobalMatrix(mat)
                }
                it.parent = targetNode
                pathes += Move(oldIndex = oldIndex,
                        oldParent = oldParent,
                        newIndex = targetNode.childs.indexOf(it),
                        newParent = targetNode,
                        node = it
                )
                if (it is Spatial) {
                    it.setGlobalTransform(mat)
                }
                if (it is Spatial2D) {
                    it.setGlobalTransform(mat)
                }
                Unit
            }
            if (dl.childIndex >= 0) {
                pathes.forEachIndexed { index, node ->
                    val index = minOf(dl.childIndex + index, targetNode.childs.lastIndex)
                    targetNode.setChildIndex(node.node, index)
                    node.newIndex = index
                }
            }
            pathes.forEach {
                model.move(tree, it.node, it.oldIndex, it.oldParent, it.newIndex, it.newParent)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            throw e
        }
        return true
    }
}