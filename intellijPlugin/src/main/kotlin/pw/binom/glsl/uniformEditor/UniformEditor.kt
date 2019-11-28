package pw.binom.glsl.uniformEditor

import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.tree.IElementType
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import mogot.Engine
import mogot.gl.Shader
import pw.binom.FlexLayout
import pw.binom.ShaderEditViewer
import pw.binom.appendTo
import pw.binom.glsl.psi.GLSLGlobalArrayDef
import pw.binom.glsl.psi.GLSLGlobalVarDefinition
import pw.binom.glsl.psi.GLSLTypes
import pw.binom.glsl.psi.GLSLVarDefinitionExp
import java.awt.BorderLayout
import java.awt.FlowLayout
import javax.swing.JComponent


class UniformEditor(private val document: Document, val shaderEditViewer: ShaderEditViewer, private val file: PsiFile) : JBPanel<JBPanel<*>>() {

    interface EditorFactory {
        fun create(project: Project, shaderEditViewer: ShaderEditViewer, document: Document, uniform: Uniform): Editor?

        companion object {
            val factories = listOf(ImageEditorFactory, FloatEditorFactory, Vec4EditorFactory)
        }
    }

    fun reinit() {
        components.forEach {
            it.reinit()
        }
    }

    interface Editor {
        fun isEquals(uniform: Uniform): Boolean
        fun apply(engine: Engine, shader: Shader)
        val component: JComponent
        fun reinit()
    }

    fun apply(engine: Engine) {
        components.forEach {
            it.apply(engine, shaderEditViewer.material.shader)
        }
    }

    class Uniform(val type: String, val size: String?, val name: String) {
        override fun equals(other: Any?): Boolean {
            val v = (other as? Uniform) ?: return false
            return v.name == name && v.type == type
        }
    }

    private val components = ArrayList<Editor>()

    private fun refreshUniforms(uniforms: List<Uniform>) {
        val forCreate = uniforms.filter { un ->
            components.all { !it.isEquals(un) }
        }.map { un ->
            EditorFactory.factories.asSequence()
                    .map { it.create(file.project, shaderEditViewer, document, un) }
                    .filterNotNull()
                    .firstOrNull()
        }.filterNotNull()

        val forRemove = (components + forCreate).filter {
            !uniforms.any { un -> it.isEquals(un) }
        }
        components -= forRemove
        components += forCreate

        forCreate.forEach {
            it.component.appendTo(flex)
            componentPanel.add(it.component, FlowLayout.LEFT)
        }

        forRemove.forEach {
            componentPanel.remove(it.component)
        }
    }

    private val componentPanel = JBPanel<JBPanel<*>>()
    private val scroll = JBScrollPane(componentPanel)

    private fun refresh() {
        val uniforms = file.asSequence().map { it as? GLSLGlobalVarDefinition }.filterNotNull()
                .filter { it.text.startsWith("uniform ") }
                .map {
                    val variable = it.varDefinitionExp
                    val array = it.array
                    Uniform(
                            type = variable.varType,
                            name = variable.name,
                            size = array?.text?.removePrefix("[")?.removeSuffix("]")
                    )
                }.toList()

        refreshUniforms(uniforms)
    }

    private val flex = FlexLayout(componentPanel, direction = FlexLayout.Direction.COLUMN)

    init {
        layout = BorderLayout()
        add(scroll, BorderLayout.CENTER)

        document.addDocumentListener(object : DocumentListener {
            override fun documentChanged(event: DocumentEvent) {
                refresh()
            }
        })

        refresh()
    }
}

fun PsiElement.childIterator() = object : Iterator<PsiElement> {
    var current: PsiElement? = firstChild
    override fun hasNext(): Boolean = current != null

    override fun next(): PsiElement {
        val p = current ?: throw NoSuchElementException()
        current = current?.nextSibling
        return p
    }
}

fun PsiElement.asSequence() = object : Sequence<PsiElement> {
    override fun iterator(): Iterator<PsiElement> = childIterator()
}

fun <T : PsiElement> Sequence<T>.withoutWhiteSpace() = filter { it !is PsiWhiteSpace }
fun <T : PsiElement> Sequence<T>.byType(type: IElementType) = filter { it.node.elementType == type }
fun <T : PsiElement> Sequence<T>.byType(type: (IElementType) -> Boolean) = filter { type(it.node.elementType) }

val GLSLVarDefinitionExp.varType
    get() = asSequence().byType { it == GLSLTypes.ID || it == GLSLTypes.PRIMITIVE }.first().text

val GLSLVarDefinitionExp.name
    get() = asSequence().byType(GLSLTypes.ID).last().text

val GLSLGlobalVarDefinition.variable
    get() = asSequence().map { it as? GLSLVarDefinitionExp }.filterNotNull().first()

val GLSLGlobalVarDefinition.array
    get() = asSequence().map { it as? GLSLGlobalArrayDef }.filterNotNull().firstOrNull()

val PsiElement.type
    get() = node.elementType//(this as? TreeElement)?.elementType