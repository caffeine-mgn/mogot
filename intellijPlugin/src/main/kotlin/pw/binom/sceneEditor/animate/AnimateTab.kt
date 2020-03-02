package pw.binom.sceneEditor.animate

import com.intellij.ui.JBSplitter
import com.intellij.ui.components.JBScrollPane
import pw.binom.sceneEditor.NodeService
import pw.binom.sceneEditor.SceneEditor
import pw.binom.sceneEditor.properties.Panel
import pw.binom.ui.AnimateFrameView
import pw.binom.ui.AnimatePropertyView
import pw.binom.ui.NodeFieldDataFlavor
import java.awt.BorderLayout
import java.awt.dnd.*

private val emptyModel = object : AnimatePropertyView.Model, AnimateFrameView.Model {
    override val nodes: List<AnimatePropertyView.Node>
        get() = emptyList()
    override val frameCount: Int
        get() = 100
    override val frameInSeconds: Int
        get() = 60
    override val lineCount: Int
        get() = 0

    override fun line(index: Int): AnimateFrameView.FrameLine {
        throw IllegalArgumentException()
    }

}

private class AddFrameLineDropListener : DropTargetListener {
    override fun dropActionChanged(dtde: DropTargetDragEvent) {

    }

    override fun drop(dtde: DropTargetDropEvent) {
        val fields = dtde.transferable.getTransferData(NodeFieldDataFlavor) as List<NodeService.Field<*>>
        println("Fields: ${fields.map { it.displayName }}")
    }

    override fun dragOver(dtde: DropTargetDragEvent) {
    }

    override fun dragExit(dte: DropTargetEvent) {
    }

    override fun dragEnter(dtde: DropTargetDragEvent) {
        if (dtde.transferable.isDataFlavorSupported(NodeFieldDataFlavor)) {
            dtde.acceptDrag(DnDConstants.ACTION_COPY)
        } else {
            dtde.rejectDrag()
        }
    }

}

class AnimateTab(val editor: SceneEditor) : Panel() {
    private val splitter = JBSplitter(false, 0.3f)

    private val propertyView = AnimatePropertyView()
    private val frameView = AnimateFrameView()
    private val scroll = JBScrollPane(frameView)
    private val dropListener = AddFrameLineDropListener()

    init {
        layout = BorderLayout()
        splitter.firstComponent = propertyView
        splitter.secondComponent = scroll
        add(splitter, BorderLayout.CENTER)

        propertyView.model = emptyModel
        frameView.model = emptyModel

        dropTarget = DropTarget(this, DnDConstants.ACTION_MOVE, dropListener, true)
    }
}