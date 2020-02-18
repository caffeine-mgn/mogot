package pw.binom.sceneEditor

import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.dnd.DropTarget
import java.awt.dnd.DropTargetDragEvent
import java.awt.dnd.DropTargetDropEvent
import java.awt.dnd.DropTargetEvent
import java.io.File
import javax.swing.TransferHandler
/*
class SceneDropTarget(val view: SceneEditorView){

}

class SceneTransferHandler(val view: SceneEditorView) : TransferHandler() {

    val dropTarget = object : DropTarget() {
        override fun dragOver(dtde: DropTargetDragEvent) {
            println("Drag over")
            super.dragOver(dtde)
        }

        override fun dragEnter(dtde: DropTargetDragEvent) {
            val file = getFile(dtde.transferable)
            if (file == null) {
                dtde.rejectDrag()
                return
            }
            dtde.transferable
            super.dragEnter(dtde)
        }

        override fun dragExit(dte: DropTargetEvent) {
            println("dragExit")
            super.dragExit(dte)
        }

        override fun drop(dtde: DropTargetDropEvent) {
            println("drop!")
            super.drop(dtde)
        }
    }

    init {
        view.dropTarget = dropTarget
    }



    override fun canImport(support: TransferSupport): Boolean {
        if (!support.isDrop)
            return false
        val transferable = support.transferable
        return getFile(transferable) != null
    }
}
 */