package pw.binom.sceneEditor.dragdrop

import com.intellij.openapi.vfs.LocalFileSystem
import mogot.waitFrame
import pw.binom.sceneEditor.SceneEditorView
import pw.binom.sceneEditor.loadTexture
import pw.binom.sceneEditor.nodeController.EditableSprite
import pw.binom.sceneEditor.nodeController.Sprite2DCreator
import pw.binom.sceneEditor.nodeController.Sprite2DService
import java.awt.MouseInfo
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.dnd.DropTarget
import java.awt.dnd.DropTargetDragEvent
import java.awt.dnd.DropTargetDropEvent
import java.awt.dnd.DropTargetEvent
import java.io.File

class SceneDropTarget(val view: SceneEditorView) : DropTarget() {

    private var currentFile: File? = null
    private var currentSprite: EditableSprite? = null

    override fun dragOver(dtde: DropTargetDragEvent) {
        if (currentFile == null)
            dtde.rejectDrag()
        if (currentSprite != null) {
            val viewLocation = view.locationOnScreen
            val mouseLocation = MouseInfo.getPointerInfo().location
            view.editorCamera2D.screenToWorld(
                    mouseLocation.x - viewLocation.x,
                    mouseLocation.y - viewLocation.y,
                    currentSprite!!.position
            )
        }
        super.dragOver(dtde)
    }

    override fun dragEnter(dtde: DropTargetDragEvent) {
        val file = getFile(dtde.transferable)
        if (file == null) {
            dtde.rejectDrag()
            currentFile = null
            return
        }
        if (view.mode == SceneEditorView.Mode.D2) {
            currentSprite = Sprite2DCreator.create(view) as EditableSprite
            currentSprite!!.parent = view.sceneRoot
            val texFile = LocalFileSystem.getInstance().findFileByIoFile(file)!!
            view.engine.waitFrame {
                val tex = view.engine.resources.loadTexture(texFile)
                currentSprite!!.textureFile = tex
                currentSprite!!.size.set(tex.gl.width.toFloat(), tex.gl.height.toFloat())
            }
            currentFile = file
        }
    }

    override fun dragExit(dte: DropTargetEvent) {
        if (currentFile == null)
            return
        println("dragExit")
        if (currentSprite != null) {
            Sprite2DService.delete(view, currentSprite!!)
            currentSprite!!.free()
        }
        super.dragExit(dte)
    }

    override fun drop(dtde: DropTargetDropEvent) {
        if (currentFile == null)
            dtde.rejectDrop()

        if (currentSprite != null) {
            view.editor1.sceneStruct.model.created(view.editor1.sceneStruct.tree, currentSprite!!)
        }

        currentSprite = null
        currentFile = null
        super.drop(dtde)
    }

    private fun getFile(transferable: Transferable): File? {
        if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            val data = transferable.getTransferData(DataFlavor.javaFileListFlavor) as? List<Any?> ?: return null
            if (data.size != 1) {
                return null
            }
            val file = (data.first() as? File) ?: return null
            if (file.extension.toLowerCase() != "png")
                return null

            return file
        }
        return null
    }
}