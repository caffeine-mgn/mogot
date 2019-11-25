package pw.binom.glsl

import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.tree.IElementType
import pw.binom.glsl.psi.GLSLGlobalArrayDef
import pw.binom.glsl.psi.GLSLGlobalVarDefinition
import pw.binom.glsl.psi.GLSLTypes
import pw.binom.glsl.psi.GLSLVarDefinitionExp
import javax.swing.JButton
import javax.swing.JPanel


class UniformEditor(private val document: Document, private val file: PsiFile) : JPanel() {

    abstract interface Editor

    inner abstract class ImageSelector : JButton(), Editor {
        init {
            addActionListener {
                FileChooser.chooseFiles(
                        FileChooserDescriptor(
                                true,
                                false,
                                false,
                                false,
                                false,
                                false
                        ),
                        file.project,
                        null
                )
            }
        }
    }

    init {
        document.addDocumentListener(object : DocumentListener {
            override fun documentChanged(event: DocumentEvent) {
                val uniforms = file.asSequence().map { it as? GLSLGlobalVarDefinition }.filterNotNull().forEach {
                    val variable = it.varDefinitionExp
                    val array = it.array
                    println("->${variable.varType}  ${variable.name}${array?.text}   (${it.node.text})")
                }
            }
        })
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