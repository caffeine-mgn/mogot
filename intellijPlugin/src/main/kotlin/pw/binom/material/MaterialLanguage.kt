package pw.binom.material

import com.intellij.lang.Language
import com.intellij.openapi.fileEditor.*
import com.intellij.openapi.fileEditor.impl.text.TextEditorProvider
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ToolWindowAnchor
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.psi.tree.IElementType
import javax.swing.Icon

object MaterialLanguage : Language("Material") {
    val ICON = IconLoader.getIcon("/fbx.png")

    override fun getAssociatedFileType(): LanguageFileType? = MaterialFileType
}

object MaterialFileType : LanguageFileType(MaterialLanguage) {
    override fun getCharset(file: VirtualFile, content: ByteArray): String? = null

    override fun isReadOnly(): Boolean = false

    override fun getIcon(): Icon? = MaterialLanguage.ICON

    override fun getName(): String = "MAT"

    override fun getDefaultExtension(): String = "mat"

    override fun getDescription(): String = "Material File"
}

class ShaderTokenType(debugName: String) : IElementType(debugName, MaterialLanguage)
abstract class ShaderElementType(debugName: String) : IElementType(debugName, MaterialLanguage)

class MaterialFileEditorProvider : FileEditorProvider, DumbAware {
    override fun getEditorTypeId(): String = "MaterialEditor"

    override fun accept(project: Project, file: VirtualFile): Boolean {

        val r = when (file.extension?.toLowerCase()) {
            "mat", "shr" -> true
            else -> false
        }
        println("MaterialFileEditorProvider accept  ${file.extension}   $file -> $r")
        return r
    }

    override fun createEditor(project: Project, file: VirtualFile): FileEditor {
        val textEditor = TextEditorProvider().createEditor(project, file) as TextEditor
        val editor = when (file.extension?.toLowerCase()) {
            "mat" -> MaterialFileEditor2(project, textEditor)
            else -> textEditor
        }
        println("Create Editor for: ${file.extension?.toLowerCase()} -> ${editor::class.java.name}")
        return editor
    }

    override fun getPolicy(): FileEditorPolicy = FileEditorPolicy.HIDE_DEFAULT_EDITOR
}

private val MATIREAL_VIEW_TOOL_WINDOW = "MatirealView"
private val MATIREAL_PROPERTIES_TOOL_WINDOW = "MatirealProperties"

private class MaterialFileEditor2(val project: Project, val fileEditor: TextEditor) : TextEditor by fileEditor {

    companion object {
        private var viewer: MaterialViewer? = null
        fun viewer(project: Project): MaterialViewer {
            if (viewer == null)
                viewer = MaterialViewer(project)
            return viewer!!
        }
    }

    var shaderEditViewer = ToolWindowManager.getInstance(project).getToolWindow(MATIREAL_VIEW_TOOL_WINDOW)
            ?: run {
                val toolWin = ToolWindowManager.getInstance(project).registerToolWindow(
                        MATIREAL_VIEW_TOOL_WINDOW,
                        false,
                        ToolWindowAnchor.RIGHT
                )
                val content = toolWin.contentManager.factory.createContent(viewer(project), "", false)
                toolWin.contentManager.addContent(content)
                toolWin
            }

    //    val document = FileDocumentManager.getInstance().getDocument(fileEditor.file!!)!!
    override fun deselectNotify() {
        viewer(project).file = null
        fileEditor.deselectNotify()
    }

    override fun selectNotify() {
        fileEditor.selectNotify()
        viewer(project).file = fileEditor.file
    }
}
/*
class MaterialFileEditor(val project: Project,
                         private val sourceFile: VirtualFile) : FileEditor {

    override fun isModified(): Boolean = true
    private val _module
        get() = ModuleUtil.findModuleForFile(sourceFile, project)

    val resolver: ModuleResolver
        get() = ModuleHolder.getInstance(ModuleUtil.findModuleForFile(sourceFile, project)!!).resolver

    val holder = ModuleHolder.getInstance(_module!!)

    val document = FileDocumentManager.getInstance().getDocument(sourceFile)!!
    var shaderEditViewer = ToolWindowManager.getInstance(project).getToolWindow(MATIREAL_VIEW_TOOL_WINDOW)
            ?: ToolWindowManager.getInstance(project).registerToolWindow(
                    MATIREAL_VIEW_TOOL_WINDOW,
                    false,
                    ToolWindowAnchor.RIGHT
            )

    val materialViewer = MaterialViewer(this)
    val uniformEditor = UniformEditor(this)
    val psifile = PsiManager.getInstance(project).findFile(sourceFile)!!

    override fun getFile(): VirtualFile? = sourceFile
    private val propertyChangeListeners = ArrayList<PropertyChangeListener>()

    override fun addPropertyChangeListener(listener: PropertyChangeListener) {
        propertyChangeListeners += listener
    }

    override fun getName(): String = "Material Editor"

    private var state: FileEditorState? = null

    override fun setState(state: FileEditorState) {
        this.state = state
    }

    val editor = EditorFactory.getInstance().createEditor(document, project, sourceFile, false) as EditorImpl
    private val component = run {
        editor.component
    }

    fun refresh() {
        fun setUniform(type: TypeDesc, name: String, value: String?) {
            if (type is SingleType && type.clazz.name == "sampler2D") {
                val texture = value
                        ?.let {
                            sourceFile.parent.findFileByRelativePath(value)
                                    ?.let { materialViewer.engine.resources.loadTexture(it) }
                        }
                materialViewer.set(name, texture)
            }

            if (type is SingleType && type.clazz.name == "float") {
                materialViewer.set(name, value?.removeSuffix("f")?.toFloat())
            }

            if (type is SingleType && type.clazz.name == "int") {
                materialViewer.set(name, value?.toFloat())
            }

            if (type is SingleType && type.clazz.name == "bool") {
                materialViewer.set(name, value == "true")
            }

            if (type is SingleType && type.clazz.name == "vec3") {
                val vector = value?.let {
                    it.split(',').map { it.trim().toFloat() }.let {
                        Vector3f(it.getOrNull(0) ?: 0f, it.getOrNull(1) ?: 0f, it.getOrNull(1) ?: 0f)
                    }
                }
                materialViewer.set(name, vector)
            }

            if (type is SingleType && type.clazz.name == "vec4") {
                val vector = value?.let {
                    it.split(',').map { it.trim().toFloat() }.let {
                        Vector4f(
                                it.getOrNull(0) ?: 0f,
                                it.getOrNull(1) ?: 0f,
                                it.getOrNull(2) ?: 0f,
                                it.getOrNull(3) ?: 0f
                        )
                    }
                }
                materialViewer.set(name, vector)
            }
        }

        try {
            val module = SourceModule(holder.getRelativePath(sourceFile))
            val parser = Parser(module, StringReader(document.text))
            val compiler = Compiler(parser, module, resolver)
            uniformEditor.update(compiler)
            val gen = GLES300Generator.mix(listOf(compiler))
            materialViewer.setShader(gen.vp, gen.fp)
            compiler.module.properties.asSequence()
                    .mapNotNull {
                        val prop = it.property ?: return@mapNotNull null
                        it to prop.value
                    }
                    .forEach {
                        setUniform(it.first.type, it.first.name, it.second)
                    }
            println("OK")
        } catch (e: Throwable) {
            println("ERROR: ${e.message}")
            e.printStackTrace()
        }
    }

    init {

        document.addDocumentListener(object : DocumentListener {
            override fun documentChanged(event: DocumentEvent) {
                refresh()
            }
        })


    }

    override fun getComponent(): JComponent = component

    override fun getPreferredFocusedComponent(): JComponent? = component

    override fun <T : Any?> getUserData(key: Key<T>): T? = userData[key] as T?

    fun ToolWindow.useContent(component: JComponent) {
        contentManager.removeAllContents(true)
        val c = contentManager.factory.createContent(component, "", false)
        contentManager.addContent(c)
    }

    override fun selectNotify() {
        shaderEditViewer.useContent(materialViewer)
//        run {
//            val c = shaderEditViewer.contentManager.contents.find { it.component is MaterialViewer }
//            if (c == null) {
//                val content = shaderEditViewer.contentManager.factory.createContent(materialViewer, "", false)
//                shaderEditViewer.contentManager.addContent(content)
//            }
//        }
//        shaderEditViewer.setAvailable(true, null)
    }

    override fun deselectNotify() {
        shaderEditViewer.contentManager.removeAllContents(true)
    }

    private val userData = HashMap<Key<*>, Any?>()

    override fun <T : Any?> putUserData(key: Key<T>, value: T?) {
        userData[key] = value
    }

    override fun getCurrentLocation(): FileEditorLocation? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getBackgroundHighlighter(): BackgroundEditorHighlighter? {
        return null
    }

    override fun isValid(): Boolean = true

    override fun removePropertyChangeListener(listener: PropertyChangeListener) {
        propertyChangeListeners -= listener
    }

    override fun dispose() {
        editor.disposable.dispose()
        materialViewer.destroy()
    }

}
*/