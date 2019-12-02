package pw.binom

import com.intellij.codeHighlighting.BackgroundEditorHighlighter
import com.intellij.lang.Language
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.editor.highlighter.EditorHighlighterFactory
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.openapi.fileEditor.*
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.psi.tree.IElementType
import com.intellij.ui.JBSplitter
import pw.binom.glsl.uniformEditor.UniformEditor
import java.awt.BorderLayout
import java.awt.Color
import java.awt.FlowLayout
import java.beans.PropertyChangeListener
import javax.swing.Icon
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel

object ShaderLanguage : Language("Shader") {
    val ICON = IconLoader.getIcon("/fbx.png")

    override fun getAssociatedFileType(): LanguageFileType? = ShaderFileType
}

object ShaderFileType : LanguageFileType(ShaderLanguage) {
    override fun getCharset(file: VirtualFile, content: ByteArray): String? = null

    override fun isReadOnly(): Boolean = false

    override fun getIcon(): Icon? = ShaderLanguage.ICON

    override fun getName(): String = "SHR"

    override fun getDefaultExtension(): String = "shr"

    override fun getDescription(): String = "Shader File"
}

class ShaderTokenType(debugName: String) : IElementType(debugName, ShaderLanguage)
abstract class ShaderElementType(debugName: String) : IElementType(debugName, ShaderLanguage)

class ShaderFileEditorProvider : FileEditorProvider {
    override fun getEditorTypeId(): String = "ShaderEditor"

    override fun accept(project: Project, file: VirtualFile): Boolean {
        println("accept  ${file.extension}   $file")
        return file.extension?.toLowerCase() == "shr"
    }

    override fun createEditor(project: Project, file: VirtualFile): FileEditor = ShaderFileEditor(project, file)

    override fun getPolicy(): FileEditorPolicy = FileEditorPolicy.HIDE_DEFAULT_EDITOR
}

class ShaderFileEditor(private val project: Project,
                       private val sourceFile: VirtualFile) : FileEditor {
    override fun isModified(): Boolean = true

    private val splitSize = Key<Int>("SPLIT")

    override fun getFile(): VirtualFile? = sourceFile
    private val propertyChangeListeners = ArrayList<PropertyChangeListener>()

    override fun addPropertyChangeListener(listener: PropertyChangeListener) {
        propertyChangeListeners += listener
    }

    override fun getName(): String = "FBX Viewer"

    private var state: FileEditorState? = null

    override fun setState(state: FileEditorState) {
        this.state = state
    }

    private val view = ShaderEditViewer()

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

    private val component = run {
        val file = PsiManager.getInstance(project).findFile(sourceFile)!!
//        val file = PsiFileFactory.getInstance(project).createFileFromText(ShaderLanguage, sourceFile.name)
        val document = FileDocumentManager.getInstance().getDocument(sourceFile)!!

        val language = ShaderLanguage//Language.findLanguageByID("HTML");
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
        panel
    }

    init {
    }

    override fun getComponent(): JComponent = component

    override fun getPreferredFocusedComponent(): JComponent? = component

    override fun <T : Any?> getUserData(key: Key<T>): T? = userData[key] as T?

    override fun selectNotify() {
    }

    private val userData = HashMap<Key<*>, Any?>()

    override fun <T : Any?> putUserData(key: Key<T>, value: T?) {
        userData[key] = value
    }

    override fun getCurrentLocation(): FileEditorLocation? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deselectNotify() {
    }

    override fun getBackgroundHighlighter(): BackgroundEditorHighlighter? {
        return null
    }

    override fun isValid(): Boolean = true

    override fun removePropertyChangeListener(listener: PropertyChangeListener) {
        propertyChangeListeners -= listener
    }

    override fun dispose() {
        view.destroy()
    }

}

class FlatBtn(text: String) : JButton(text) {
    init {
//        isBorderPainted = false
//        isFocusPainted = false
//        isContentAreaFilled = false
    }
}