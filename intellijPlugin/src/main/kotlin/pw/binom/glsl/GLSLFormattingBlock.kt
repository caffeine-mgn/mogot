package pw.binom.glsl

import com.intellij.formatting.*
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.formatter.common.AbstractBlock
import pw.binom.glsl.psi.GLSLFunctionDefinition
import pw.binom.glsl.psi.GLSLStatement
import pw.binom.glsl.psi.GLSLStructDefinition
import java.util.*

val PsiElement.type
    get() = node.elementType//(this as? TreeElement)?.elementType

class GLSLFormattingBlock(node: ASTNode, wrap: Wrap?, alignment: Alignment?, val settings: CodeStyleSettings, val spacingBuilder: SpacingBuilder) : AbstractBlock(node, wrap, alignment) {
    override fun isLeaf(): Boolean {
        return myNode.firstChildNode == null
    }

    override fun getSpacing(child1: Block?, child2: Block): Spacing? =
            spacingBuilder.getSpacing(this, child1, child2)

    private fun canBeCorrectBlock(child: ASTNode): Boolean {
        return child.text.isNotBlank()
    }

    override fun getIndent(): Indent? {
        val psiElement: PsiElement = myNode.psi
        val r = when (psiElement) {
//            is GLSLStatement,
            is GLSLStructDefinition,
            is GLSLFunctionDefinition -> Indent.getNoneIndent()
            is GLSLStatement -> Indent.getNormalIndent()
            else -> Indent.getNoneIndent()
        }
//        println("For ${psiElement.type} -> $r")
        return r
    }

    override fun getChildIndent(): Indent? {
        val psiElement: PsiElement = myNode.psi
        // Note: this cannot use isIndentableBlock because when pressing enter after a right brace, the psiElement in question is
        //       a GLSLTypeDefinition instead of a GLSLStructDeclaration
        val r = when (psiElement) {
//            is GLSLStatement,
            is GLSLStructDefinition,
            is GLSLFunctionDefinition -> Indent.getNormalIndent()
            is GLSLStatement -> Indent.getSpaceIndent(1)
            else -> Indent.getNoneIndent()
        }

        println("For ${psiElement.type} -> $r")
        return r
    }

    override fun buildChildren(): MutableList<Block> {
        val blocks: MutableList<Block> = ArrayList()
        var child = myNode.firstChildNode

        while (child != null) {
            if (canBeCorrectBlock(child)) {
                blocks.add(GLSLFormattingBlock(child, null, null, settings, spacingBuilder))
            }
            child = child.treeNext
        }
        return blocks
    }

    override fun toString(): String {
        return "${myNode.text} ${myNode.elementType}"
    }
}