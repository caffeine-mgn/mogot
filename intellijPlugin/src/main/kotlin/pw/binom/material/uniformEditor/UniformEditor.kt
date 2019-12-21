package pw.binom.material.uniformEditor

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.tree.IElementType
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import mogot.Engine
import mogot.gl.Shader
import pw.binom.FlexLayout
import pw.binom.appendTo
import pw.binom.glsl.psi.GLSLTypes
import pw.binom.glsl.psi.GLSLVarDefinitionExp
import pw.binom.material.MaterialFileEditor
import pw.binom.material.compiler.Compiler
import java.awt.BorderLayout
import java.awt.FlowLayout
import javax.swing.JComponent


class UniformEditor(val editor: MaterialFileEditor) : JBPanel<JBPanel<*>>() {

    interface EditorFactory {
        fun create(uniformEditor: UniformEditor, uniform: Uniform): Editor?

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
        val uniform: Uniform
        fun isEquals(uniform: Uniform): Boolean
        fun apply(engine: Engine, shader: Shader)
        val component: JComponent
        fun reinit()
        fun addChangeListener(listener: () -> Unit)
        val value: Any?
    }

    fun apply() {
        components.forEach {
            editor.materialViewer.set(it.uniform.name, it.value)
        }
    }

    /*
        fun apply(engine: Engine) {
            components.forEach {
                it.apply(engine, shaderEditViewer.material.shader)
            }
        }
    */
    fun update(compiler: Compiler) {
        val uniforms = compiler.properties.map {
            Uniform(
                    type = it.key.type.clazz.name,
                    name = it.key.name,
                    size = null,
                    max = it.value["max"]?.toFloatOrNull(),
                    min = it.value["min"]?.toFloatOrNull(),
                    step = it.value["step"]?.toFloatOrNull()
            )
        }
        refreshUniforms(uniforms)
    }

    class Uniform(val type: String, val size: String?, val name: String, val min: Float?, val max: Float?, val step: Float?) {
        override fun equals(other: Any?): Boolean {
            val v = (other as? Uniform) ?: return false
            return v.name == name && v.type == type
        }

        override fun hashCode(): Int = name.hashCode() + type.hashCode()
    }

    private val components = ArrayList<Editor>()

    private fun refreshUniforms(uniforms: List<Uniform>) {
        val forCreate = uniforms.filter { un ->
            components.all { !it.isEquals(un) }
        }.map { un ->
            EditorFactory.factories.asSequence()
                    .map { it.create(this, un) }
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
            editor.materialViewer.set(it.uniform.name, it.value)
            it.addChangeListener {
                apply()
                editor.materialViewer.repaint()
            }
        }

        forRemove.forEach {
            editor.materialViewer.set(it.uniform.name, null)
            componentPanel.remove(it.component)
        }
    }

    private val componentPanel = JBPanel<JBPanel<*>>()
    private val scroll = JBScrollPane(componentPanel)
    /*
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
    */
    private val flex = FlexLayout(componentPanel, direction = FlexLayout.Direction.COLUMN)

    init {
        layout = BorderLayout()
        add(scroll, BorderLayout.CENTER)
/*
        document.addDocumentListener(object : DocumentListener {
            override fun documentChanged(event: DocumentEvent) {
                refresh()
            }
        })

        refresh()
        */
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

val PsiElement.type
    get() = node.elementType//(this as? TreeElement)?.elementType