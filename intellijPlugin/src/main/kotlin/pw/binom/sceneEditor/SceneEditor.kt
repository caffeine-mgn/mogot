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
import pw.binom.sceneEditor.struct.SceneStruct
import java.beans.PropertyChangeListener
import javax.swing.JButton
import javax.swing.JComponent

private var privateCurrentEditor: SceneEditor? = null

class SceneEditor(val project: Project,
                  private val sourceFile: VirtualFile) : FileEditor, DocumentReferenceProvider {

    companion object {
        val currentSceneEditor
            get() = privateCurrentEditor
    }

    private val SCENE_TOOL_WINDOW = "Scene"
    private val PROPERTIES_TOOL_WINDOW = "Properties"

    private val undoManager = UndoManager.getInstance(project)

    val structToolWindow: ToolWindow
    val propertyToolWindow: ToolWindow

    private val properties = HashMap<PropertyFactory, Property>()

    fun getProperty(factory: PropertyFactory): Property = properties.getOrPut(factory) { factory.create(viewer.view) }
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
        val r = _contentRoots
                ?.asSequence()
                ?.mapNotNull {
                    it.findFileByRelativePath(path)
                }
                ?.firstOrNull()
        if (r == null) {
            println("Can't resolve $path roots: ${_contentRoots?.toList()}")
            return LocalFileSystem.getInstance().findFileByPath(path)
        }
        return r
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
    private var _viewer: ViewPlane? = null
    val viewer
        get() = _viewer!!
    lateinit var sceneStruct: SceneStruct
    val propertyTool = PropertyToolWindow(this)


    init {
        if (_module == null) {

        } else {
            _viewer = ViewPlane(this, project, sourceFile, 20)
            sceneStruct = SceneStruct(_viewer!!.view)
            viewer.view.eventSelectChanged.on {
                propertyTool.setNodes(viewer.view.selected)
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
    }

    override fun selectNotify() {
        privateCurrentEditor = this
        viewer.view.startRender()
        println("selectNotify")
        structToolWindow.useContent(sceneStruct)
        propertyToolWindow.useContent(propertyTool)
//        structToolWindow.setAvailable(true, null)
//        propertyToolWindow.setAvailable(true, null)
    }

    override fun deselectNotify() {
        structToolWindow.contentManager.removeAllContents(true)
        propertyToolWindow.contentManager.removeAllContents(true)
        privateCurrentEditor = null
        viewer.view.stopRender()
//        println("deselectNotify")
//        structToolWindow.setAvailable(false, null)
//        propertyToolWindow.setAvailable(false, null)
    }

    override fun <T : Any?> putUserData(key: Key<T>, value: T?) {
        userData[key] = value
    }

    override fun getCurrentLocation(): FileEditorLocation? {
        return null
    }

    override fun getBackgroundHighlighter(): BackgroundEditorHighlighter? {
        return null
    }

    override fun isValid(): Boolean = true

    override fun removePropertyChangeListener(listener: PropertyChangeListener) {
    }

    //private val document = FileDocumentManager.getInstance().getDocument(sourceFile)!!
    override fun dispose() {
        _viewer?.view?.destroy()
    }

    val ref = object : DocumentReference {
        override fun getFile(): VirtualFile? = sourceFile

        override fun getDocument(): Document? = document
    }

    override fun getDocumentReferences(): MutableCollection<DocumentReference> = mutableListOf(ref)


    override fun getFile(): VirtualFile? = sourceFile
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
