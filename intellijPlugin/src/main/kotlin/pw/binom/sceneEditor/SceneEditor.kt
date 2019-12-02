package pw.binom.sceneEditor

import com.intellij.codeHighlighting.BackgroundEditorHighlighter
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.command.CommandEvent
import com.intellij.openapi.command.CommandListener
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.command.UndoConfirmationPolicy
import com.intellij.openapi.command.undo.*
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.editor.event.EditorEventMulticaster
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorLocation
import com.intellij.openapi.fileEditor.FileEditorState
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowAnchor
import com.intellij.openapi.wm.ToolWindowManager
import java.beans.PropertyChangeListener
import javax.swing.JComponent


class SceneEditor(private val project: Project,
                  private val sourceFile: VirtualFile) : FileEditor, DocumentReferenceProvider {

    private val SCENE_TOOL_WINDOW = "Scene"

    companion object {

    }

    private val undoManager = UndoManager.getInstance(project)

    val structToolWindow: ToolWindow
    private val bus = project.messageBus.connect(this)

    init {
        structToolWindow = ToolWindowManager.getInstance(project).getToolWindow(SCENE_TOOL_WINDOW)
                ?: ToolWindowManager.getInstance(project).registerToolWindow(
                        SCENE_TOOL_WINDOW,
                        false,
                        ToolWindowAnchor.RIGHT
                )

        bus.subscribe(CommandListener.TOPIC, object : CommandListener {
            override fun commandStarted(event: CommandEvent) {
                println("commandStarted $event")
            }

            override fun beforeCommandFinished(event: CommandEvent) {
                println("beforeCommandFinished $event")
            }

            override fun undoTransparentActionFinished() {
                println("undoTransparentActionFinished")
            }

            override fun commandFinished(event: CommandEvent) {
                println("commandFinished $event")
            }

            override fun beforeUndoTransparentActionFinished() {
                println("beforeUndoTransparentActionFinished")
            }

            override fun undoTransparentActionStarted() {
                println("undoTransparentActionStarted")
            }
        })
        val multicaster: EditorEventMulticaster = EditorFactory.getInstance().eventMulticaster
        multicaster.addDocumentListener(object : DocumentListener {
            override fun documentChanged(event: DocumentEvent) {
                println("documentChanged")
            }

            override fun beforeDocumentChange(event: DocumentEvent) {
                println("beforeDocumentChange")
            }
        }, this)
    }

    private val userData = HashMap<Key<*>, Any?>()

    val viewer = SceneEditorView(this, project, sourceFile)
    val sceneStruct = SceneStruct(viewer)

    private val component = viewer

    override fun isModified(): Boolean {

        return false
    }

    private val propertyChangeListeners = ArrayList<PropertyChangeListener>()
    override fun addPropertyChangeListener(listener: PropertyChangeListener) {
        propertyChangeListeners += listener
    }

    override fun getName(): String = "Scene Editor"

    override fun setState(state: FileEditorState) {
    }

    override fun getComponent(): JComponent = component
    override fun getPreferredFocusedComponent(): JComponent? = component

    override fun <T : Any?> getUserData(key: Key<T>): T? =
            userData[key] as T?

    override fun selectNotify() {
        println("selectNotify")
        save()
        //таб активен
        val c = structToolWindow.contentManager.contents.find { it.component is SceneStruct }
        if (c == null) {
            val content = structToolWindow.contentManager.factory.createContent(sceneStruct, "Scene Struct", false)
            structToolWindow.contentManager.addContent(content)
            sceneStruct
        } else c.component as SceneStruct

        structToolWindow.setAvailable(true, null)
    }

    override fun <T : Any?> putUserData(key: Key<T>, value: T?) {
        userData[key] = value
    }

    override fun getCurrentLocation(): FileEditorLocation? {
        return null
    }

    override fun deselectNotify() {
        println("deselectNotify")
        structToolWindow.setAvailable(false, null)
    }

    override fun getBackgroundHighlighter(): BackgroundEditorHighlighter? {
        return null
    }

    override fun isValid(): Boolean = true

    override fun removePropertyChangeListener(listener: PropertyChangeListener) {
    }

    private val document = FileDocumentManager.getInstance().getDocument(sourceFile)!!
    override fun dispose() {
        component.destroy()
    }

    val ref = object : DocumentReference {
        override fun getFile(): VirtualFile? = sourceFile

        override fun getDocument(): Document? = document
    }

    override fun getDocumentReferences(): MutableCollection<DocumentReference> = mutableListOf(ref)


    override fun getFile(): VirtualFile? = sourceFile

    fun save() {
        val cmd = object : BasicUndoableAction(ref), Runnable {
            override fun redo() {
                println("--->REDO")
            }

            override fun undo() {
                println("--->UNDO")
            }

            override fun run() {
                println("ACTION!")
            }


        }
        CommandProcessor.getInstance().runUndoTransparentAction(cmd)
//        CommandProcessor.getInstance().executeCommand(project, cmd, "ACTION1", "GROUP1", UndoConfirmationPolicy.DO_NOT_REQUEST_CONFIRMATION)
    }

}

class MoveNode : UndoableAction {
    override fun redo() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun undo() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun isGlobal(): Boolean = false

    override fun getAffectedDocuments(): Array<DocumentReference>? = emptyArray()

}
