package pw.binom.ui

import mogot.math.Vector2f
import mogot.math.set
import pw.binom.sceneEditor.NodeService
import pw.binom.sceneEditor.SceneEditor
import pw.binom.sceneEditor.properties.Panel
import pw.binom.utils.common
import java.awt.Cursor
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.dnd.*
import java.io.Closeable

object NodeFieldDataFlavor : DataFlavor("x-mogot/node-field") {
    val array: Array<DataFlavor> = arrayOf(NodeFieldDataFlavor)
}

class FieldTransferable(val fields: List<NodeService.Field<*>>) : Transferable {
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

class AnimateTransferHandler(val fields: List<NodeService.Field<*>>) : DragGestureListener, DragSourceListener {
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

class EditorVec2(val sceneEditor: SceneEditor, val fields: List<NodeService.FieldVec2>) : Panel(), Closeable {

    val animateTransferHandler = AnimateTransferHandler(fields)
    val dgr = DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_MOVE, animateTransferHandler)

    private val layout = gridBagLayout()
    private val title = PropertyName(fields.first().displayName).appendTo(layout, 0, 0)
    private val editor = Vector2Value().appendTo(layout, 1, 0)

    private var enableEvents = true
    private var enableEvents2 = true
    private val closable = ArrayList<pw.binom.io.Closeable>()

    private fun refreshValues() {
        enableEvents = false
        editor.value.set(fields.asSequence().map { it.currentValue }.common)
        enableEvents = true
    }

    init {
        fields.forEach {
            closable += it.eventChange.on {
                if (enableEvents2)
                    refreshValues()
            }
            Unit
        }
        editor.eventChange.on {
            enableEvents2 = false
            val x = editor.value.x
            val y = editor.value.y
            if (enableEvents)
                when {
                    !x.isNaN() && !y.isNaN() -> {
                        val v = Vector2f(x, y)
                        fields.forEach {
                            it.currentValue = v
                        }
                    }
                    !editor.value.x.isNaN() ->
                        fields.forEach {
                            it.currentValue = Vector2f(x, it.currentValue.y)
                        }
                    !editor.value.y.isNaN() ->
                        fields.forEach {
                            it.currentValue = Vector2f(it.currentValue.x, y)
                        }

                }
            enableEvents2 = true
        }
        refreshValues()
    }

    override fun close() {
        closable.forEach {
            it.close()
        }
    }
}