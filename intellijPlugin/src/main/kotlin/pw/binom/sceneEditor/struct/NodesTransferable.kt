package pw.binom.sceneEditor.struct

import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException
import javax.swing.tree.DefaultMutableTreeNode
import mogot.*


class NodesTransferable(var nodes: List<Node>) : Transferable {
    @Throws(UnsupportedFlavorException::class)
    override fun getTransferData(flavor: DataFlavor): Any {
        if (!isDataFlavorSupported(flavor))
            throw UnsupportedFlavorException(flavor)
        return nodes
    }

    override fun getTransferDataFlavors(): Array<DataFlavor> {
        return TreeTransferHandler.flavors
    }

    override fun isDataFlavorSupported(flavor: DataFlavor): Boolean {
        return TreeTransferHandler.nodesFlavor == flavor
    }

}