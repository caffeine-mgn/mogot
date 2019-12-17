package pw.binom.material

import com.intellij.codeHighlighting.BackgroundEditorHighlighter
import com.intellij.codeInsight.hint.HintManager
import com.intellij.lang.Language
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.openapi.fileEditor.*
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ToolWindowAnchor
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.psi.PsiManager
import com.intellij.psi.tree.IElementType
import pw.binom.material.compiler.Compiler
import pw.binom.material.generator.gles300.GLES300Generator
import pw.binom.material.psi.Parser
import pw.binom.material.uniformEditor.UniformEditor
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

class MaterialFileEditorProvider : FileEditorProvider {
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

    var propertiesEditViewer = ToolWindowManager.getInstance(project).getToolWindow(MATIREAL_PROPERTIES_TOOL_WINDOW)
            ?: ToolWindowManager.getInstance(project).registerToolWindow(
                    MATIREAL_PROPERTIES_TOOL_WINDOW,
                    false,
                    ToolWindowAnchor.RIGHT
            )

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


    val VP = """#version 440 core

layout(location = 0) in vec3 vertexPos;
layout(location = 1) in vec3 normalList;
layout(location = 2) in vec2 vertexUV;

uniform mat4 projection;
uniform mat4 model;

uniform mat4 gl_ModelViewMatrix;
uniform mat4 gl_ProjectionMatrix;
uniform mat3 gl_NormalMatrix;
out vec2 UV;
out vec3 normal;
out vec3 vVertex;

void main() {
    mat3 normalMatrix = mat3(transpose(inverse(model)));
    normal = vec3(normalMatrix * normalList);
    gl_Position = projection * model * vec4(vertexPos, 1.0);
    //norm=normalize(vec3(gl_NormalMatrix * normalList));
    vVertex = vec3(model * vec4(vertexPos, 1.0));
    UV = vertexUV;
}"""
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
        try {
            val parser = Parser(StringReader(document.text))
            val compiler = Compiler(parser)
            uniformEditor.update(compiler)
            val gen = GLES300Generator(compiler)
            materialViewer.setShader(gen.vp, gen.fp)
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

        refresh()
    }

    override fun getComponent(): JComponent = component

    override fun getPreferredFocusedComponent(): JComponent? = component

    override fun <T : Any?> getUserData(key: Key<T>): T? = userData[key] as T?

    override fun selectNotify() {
        run {
            val c = shaderEditViewer.contentManager.contents.find { it.component is MaterialViewer }
            if (c == null) {
                val content = shaderEditViewer.contentManager.factory.createContent(materialViewer, "", false)
                shaderEditViewer.contentManager.addContent(content)
            }
        }
        run {
            val c = propertiesEditViewer.contentManager.contents.find { it.component is UniformEditor }
            if (c == null) {
                val content = propertiesEditViewer.contentManager.factory.createContent(uniformEditor, "", false)
                propertiesEditViewer.contentManager.addContent(content)
            }
        }

        shaderEditViewer.setAvailable(true, null)
        propertiesEditViewer.setAvailable(true, null)
    }

    private val userData = HashMap<Key<*>, Any?>()

    override fun <T : Any?> putUserData(key: Key<T>, value: T?) {
        userData[key] = value
    }

    override fun getCurrentLocation(): FileEditorLocation? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deselectNotify() {
        shaderEditViewer.setAvailable(false, null)
        propertiesEditViewer.setAvailable(false, null)
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