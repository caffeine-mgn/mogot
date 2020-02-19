package pw.binom.material

import com.intellij.codeHighlighting.BackgroundEditorHighlighter
import com.intellij.codeInsight.hint.HintManager
import com.intellij.lang.Language
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.openapi.fileEditor.*
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowAnchor
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.psi.PsiManager
import com.intellij.psi.tree.IElementType
import mogot.math.Vector4f
import org.joml.Vector3f
import pw.binom.material.compiler.Compiler
import pw.binom.material.compiler.SingleType
import pw.binom.material.compiler.TypeDesc
import pw.binom.material.generator.gles300.GLES300Generator
import pw.binom.material.psi.Parser
import pw.binom.material.uniformEditor.UniformEditor
import pw.binom.sceneEditor.loadTexture
import java.beans.PropertyChangeListener
import java.io.StringReader
import javax.swing.Icon
import javax.swing.JComponent

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
        println("accept  ${file.extension}   $file")
        return file.extension?.toLowerCase() == "mat"
    }

    override fun createEditor(project: Project, file: VirtualFile): FileEditor = MaterialFileEditor(project, file)

    override fun getPolicy(): FileEditorPolicy = FileEditorPolicy.HIDE_DEFAULT_EDITOR
}

private val MATIREAL_VIEW_TOOL_WINDOW = "MatirealView"
private val MATIREAL_PROPERTIES_TOOL_WINDOW = "MatirealProperties"

class MaterialFileEditor(val project: Project,
                         private val sourceFile: VirtualFile) : FileEditor {
    override fun isModified(): Boolean = true
    val document = FileDocumentManager.getInstance().getDocument(sourceFile)!!
    var shaderEditViewer = ToolWindowManager.getInstance(project).getToolWindow(MATIREAL_VIEW_TOOL_WINDOW)
            ?: ToolWindowManager.getInstance(project).registerToolWindow(
                    MATIREAL_VIEW_TOOL_WINDOW,
                    false,
                    ToolWindowAnchor.RIGHT
            )
    /*
        var propertiesEditViewer = ToolWindowManager.getInstance(project).getToolWindow(MATIREAL_PROPERTIES_TOOL_WINDOW)
                ?: ToolWindowManager.getInstance(project).registerToolWindow(
                        MATIREAL_PROPERTIES_TOOL_WINDOW,
                        false,
                        ToolWindowAnchor.RIGHT
                )
    */
    val materialViewer = MaterialViewer(this)
    val uniformEditor = UniformEditor(this)
    val psifile = PsiManager.getInstance(project).findFile(sourceFile)!!
    private val splitSize = Key<Int>("SPLIT")

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
        /*
        val file = PsiManager.getInstance(project).findFile(sourceFile)!!
//        val file = PsiFileFactory.getInstance(project).createFileFromText(ShaderLanguage, sourceFile.name)
        val document = FileDocumentManager.getInstance().getDocument(sourceFile)!!

        val language = MaterialLanguage//Language.findLanguageByID("HTML");
        val fileType = language.associatedFileType
        val myTextViewer = EditorFactory.getInstance().createEditor(document, project, sourceFile, false) as EditorImpl
        if (fileType != null)
            myTextViewer.highlighter = EditorHighlighterFactory.getInstance().createEditorHighlighter(project, fileType)
        myTextViewer.component
        //val scrollPane = JBScrollPane(myTextViewer.component)

//        val panel = JPanel()
        val viewer = view
        val splitPane = JBSplitter()
//        val splitPane = JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPane, viewer)
//        splitPane.dividerLocation = document.getUserData(splitSize) ?: 150
        val splitPane2 = JBSplitter(true)
        splitPane2.firstComponent = viewer
        val uniformEditor = UniformEditor(document, viewer, file)
        splitPane2.secondComponent = uniformEditor
        splitPane2.splitterProportionKey = "SPLIT2"
        splitPane.firstComponent = myTextViewer.component//scrollPane
        splitPane.secondComponent = splitPane2
        splitPane.splitterProportionKey = "SPLIT"
        document.addDocumentListener(object : DocumentListener {
            override fun documentChanged(event: DocumentEvent) {
                viewer.setShader(VP, document.text)
            }
        })

        viewer.addRenderListener {
            uniformEditor.apply(viewer.engine)
        }

        viewer.addInitListener {
            uniformEditor.reinit()
        }


        viewer.setShader(VP, document.text)
        viewer.repaint()
        val panel = JPanel()
        panel.layout = BorderLayout()
        panel.add(splitPane, BorderLayout.CENTER)
        panel*/
        editor.component
    }

    val hintManager = project.getComponent(HintManager::class.java)

    private fun refresh() {

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
                materialViewer.set(name, value?.toFloat())
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
            val parser = Parser(StringReader(document.text))
            val compiler = Compiler(parser)
            uniformEditor.update(compiler)
            val gen = GLES300Generator.mix(listOf(compiler))
            materialViewer.setShader(gen.vp, gen.fp)
            compiler.properties.asSequence()
                    .mapNotNull {
                        it.key to it.value["value"]
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
//        shaderEditViewer.setAvailable(false, null)
//        propertiesEditViewer.setAvailable(false, null)
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