package pw.binom.sceneEditor

import com.intellij.codeHighlighting.BackgroundEditorHighlighter
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.command.undo.*
import com.intellij.openapi.editor.Document
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
import pw.binom.sceneEditor.properties.Property
import pw.binom.sceneEditor.properties.PropertyFactory
import java.beans.PropertyChangeListener
import javax.swing.JComponent


class SceneEditor(val project: Project,
                  private val sourceFile: VirtualFile) : FileEditor, DocumentReferenceProvider {

    private val SCENE_TOOL_WINDOW = "Scene"
    private val PROPERTIES_TOOL_WINDOW = "Properties"

    companion object {

    }

    private val undoManager = UndoManager.getInstance(project)

    val structToolWindow: ToolWindow
    val propertyToolWindow: ToolWindow

    private val properties = HashMap<PropertyFactory, Property>()

    fun getProperty(factory: PropertyFactory): Property = properties.getOrPut(factory) { factory.create(viewer) }

    init {
        structToolWindow = ToolWindowManager.getInstance(project).getToolWindow(SCENE_TOOL_WINDOW)
                ?: ToolWindowManager.getInstance(project).registerToolWindow(
                        SCENE_TOOL_WINDOW,
                        false,
                        ToolWindowAnchor.RIGHT
                )

        propertyToolWindow = ToolWindowManager.getInstance(project).getToolWindow(PROPERTIES_TOOL_WINDOW)
                ?: ToolWindowManager.getInstance(project).registerToolWindow(
                        PROPERTIES_TOOL_WINDOW,
                        false,
                        ToolWindowAnchor.RIGHT
                )

/*
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
        */
        /*
        val multicaster: EditorEventMulticaster = EditorFactory.getInstance().eventMulticaster
        multicaster.addDocumentListener(object : DocumentListener {
            override fun documentChanged(event: DocumentEvent) {
                println("documentChanged")
            }

            override fun beforeDocumentChange(event: DocumentEvent) {
                println("beforeDocumentChange")
            }
        }, this)
        */
    }

    private val userData = HashMap<Key<*>, Any?>()

    val viewer = SceneEditorView(this, project, sourceFile)
    val sceneStruct = SceneStruct(viewer)
    val propertyTool = PropertyToolWindow(this)

    init {
        viewer.eventSelectChanged.on {
            propertyTool.setNodes(viewer.selected)
        }
    }

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
        run {
            val c = structToolWindow.contentManager.contents.find { it.component is SceneStruct }
            if (c == null) {
                val content = structToolWindow.contentManager.factory.createContent(sceneStruct, "", false)
                structToolWindow.contentManager.addContent(content)
                sceneStruct
            } else c.component as SceneStruct
        }

        run {
            val c = propertyToolWindow.contentManager.contents.find { it.component is PropertyToolWindow }
            if (c == null) {
                val content = propertyToolWindow.contentManager.factory.createContent(propertyTool, "", false)
                propertyToolWindow.contentManager.addContent(content)
                propertyTool
            } else c.component as PropertyToolWindow
        }

        structToolWindow.setAvailable(true, null)
        propertyToolWindow.setAvailable(true, null)
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
        propertyToolWindow.setAvailable(false, null)
    }

    override fun getBackgroundHighlighter(): BackgroundEditorHighlighter? {
        return null
    }

    override fun isValid(): Boolean = true

    override fun removePropertyChangeListener(listener: PropertyChangeListener) {
    }

    //private val document = FileDocumentManager.getInstance().getDocument(sourceFile)!!
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
