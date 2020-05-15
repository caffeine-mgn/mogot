package pw.binom.glsl

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilder
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import pw.binom.glsl.psi.GLSLTypes
import java.util.*


class GLSLFoldingBuilder : FoldingBuilder {
    override fun getPlaceholderText(node: ASTNode): String? = "{...}"

    override fun buildFoldRegions(node: ASTNode, document: Document): Array<FoldingDescriptor> {
        val descriptors = ArrayList<FoldingDescriptor>()
        appendDescriptors(node, descriptors)
        return descriptors.toTypedArray()
    }

    private fun appendDescriptors(node: ASTNode, descriptors: MutableList<FoldingDescriptor>) {
        val type = node.elementType
        val textRange = node.textRange
        //Don't add folding to 0-length nodes, crashes in new FoldingDescriptor
        if (textRange.getLength() <= 0) return
        if (type === GLSLTypes.COMMENT_BLOCK || type === GLSLTypes.BLOCK_STATEMENT) {
            descriptors.add(FoldingDescriptor(node, textRange))
        }
        var child = node.firstChildNode
        while (child != null) {
            appendDescriptors(child, descriptors)
            child = child.treeNext
        }
    }

    override fun isCollapsedByDefault(node: ASTNode): Boolean = false

}