package pw.binom.sceneEditor

import com.intellij.codeHighlighting.BackgroundEditorHighlighter
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.command.undo.*
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorLocation
import com.intellij.openapi.fileEditor.FileEditorState
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowAnchor
import com.intellij.openapi.wm.ToolWindowManager
import pw.binom.sceneEditor.properties.Property
import pw.binom.sceneEditor.properties.PropertyFactory
import java.beans.PropertyChangeListener
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel


class SceneEditor(val project: Project,
                  private val sourceFile: VirtualFile) : FileEditor, DocumentReferenceProvider {

    private val SCENE_TOOL_WINDOW = "Scene"
    private val PROPERTIES_TOOL_WINDOW = "Properties"

    private val undoManager = UndoManager.getInstance(project)

    val structToolWindow: ToolWindow
    val propertyToolWindow: ToolWindow

    private val properties = HashMap<PropertyFactory, Property>()

    fun getProperty(factory: PropertyFactory): Property = properties.getOrPut(factory) { factory.create(viewer) }
    private val _module
        get() = ModuleUtil.findModuleForFile(sourceFile, project)

    private val _contentRoots
        get() = _module?.let { ModuleRootManager.getInstance(it) }?.contentRoots

    /**
     * Return file path by relative to resource dir
     */
    fun getRelativePath(file: VirtualFile): String {
        val roots = _contentRoots ?: return file.path
        val r = roots.asSequence()
                .map {
                    if (!file.path.startsWith(it.path))
                        return@map null
                    file.path.removePrefix(it.path).removePrefix("/").removePrefix("\\")
                }.filterNotNull().firstOrNull()
        if (r == null) {
            println("Can't find relative path for $file")
            println("roots: $roots")
            return file.path
        }
        return r
    }

    /**
     * Return real file path using resource root dirs
     */
    fun findFileByRelativePath(path: String): VirtualFile? {
        return _contentRoots?.asSequence()
                ?.map {
                    it.findFileByRelativePath(path)
                }
                ?.firstOrNull() ?: LocalFileSystem.getInstance().findFileByPath(path)
    }

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
    }

    private val userData = HashMap<Key<*>, Any?>()
    private var _viewer: SceneEditorView? = null
    val viewer
        get() = _viewer!!
    lateinit var sceneStruct: SceneStruct
    val propertyTool = PropertyToolWindow(this)


    init {
        if (_module == null) {

        } else {
            _viewer = SceneEditorView(this, project, sourceFile)
            sceneStruct = SceneStruct(_viewer!!)
            viewer.eventSelectChanged.on {
                propertyTool.setNodes(viewer.selected)
            }
        }
    }

    private val component: JComponent = _viewer ?: JButton("Scene must be inside some resource folder")

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

    fun ToolWindow.useContent(component: JComponent) {
        contentManager.removeAllContents(true)
        val c = contentManager.factory.createContent(component, "", false)
        contentManager.addContent(c)

//        var c = contentManager.contents.firstOrNull()
//        if (c == null) {
//            c = contentManager.factory.createContent(component, "", false)
//            contentManager.addContent(c)
//        } else {
//            c.component = component
//        }
    }

    override fun selectNotify() {
        println("selectNotify")
        save()
        println("first!")
        structToolWindow.useContent(sceneStruct)

        println("second!")
        propertyToolWindow.useContent(propertyTool)
        /*
        //таб активен
        val c = structToolWindow.contentManager.contents.find { it.component is SceneStruct }!!.component
        run {
            val content = structToolWindow.contentManager.factory.createContent(sceneStruct, "", false)
            structToolWindow.contentManager.addContent(content)
            sceneStruct
        }

        run {
            val content = propertyToolWindow.contentManager.factory.createContent(propertyTool, "", false)
            propertyToolWindow.contentManager.addContent(content)
            propertyTool
        }
*/
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
        _viewer?.destroy()
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
