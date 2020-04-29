package pw.binom.ui

import pw.binom.sceneEditor.NodeService
import pw.binom.sceneEditor.SceneEditor
import pw.binom.sceneEditor.properties.Panel
import java.awt.Cursor
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.dnd.*
import java.io.Closeable

object NodeFieldDataFlavor : DataFlavor("x-mogot/node-field") {
    val array: Array<DataFlavor> = arrayOf(NodeFieldDataFlavor)
}

class FieldTransferable(val fields: List<NodeService.Field>) : Transferable {
    override fun getTransferData(flavor: DataFlavor?): Any? {
        if (flavor !== NodeFieldDataFlavor)
            return null
        return fields
    }

    override fun isDataFlavorSupported(flavor: DataFlavor?): Boolean {
        return flavor === NodeFieldDataFlavor
    }

    override fun getTransferDataFlavors(): Array<DataFlavor> = NodeFieldDataFlavor.array

}

class AnimateTransferHandler(val fields: List<NodeService.Field>) : DragGestureListener, DragSourceListener {
    override fun dragGestureRecognized(dge: DragGestureEvent) {
        val ds = dge.dragSource
        val transferable: Transferable = FieldTransferable(fields)
        ds.startDrag(dge, Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR), transferable, this)
    }

    override fun dropActionChanged(dsde: DragSourceDragEvent?) {
    }

    override fun dragOver(dsde: DragSourceDragEvent?) {
    }

    override fun dragExit(dse: DragSourceEvent?) {
    }

    override fun dragEnter(dsde: DragSourceDragEvent?) {
    }

    override fun dragDropEnd(dsde: DragSourceDropEvent?) {
    }
}

abstract class AbstractEditor(val sceneEditor: SceneEditor, val fields: List<NodeService.Field>):Panel(), Closeable {
    private val animateTransferHandler = AnimateTransferHandler(fields)
    private val dgr = DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_MOVE, animateTransferHandler)
    protected val closable = ArrayList<pw.binom.io.Closeable>()

    override fun close() {
        closable.forEach {
            it.close()
        }
    }
}