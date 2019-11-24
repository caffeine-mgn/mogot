package pw.binom.glsl

import com.intellij.lang.CodeDocumentationAwareCommenter
import com.intellij.psi.PsiComment
import com.intellij.psi.tree.IElementType
import pw.binom.glsl.psi.GLSLTypes

class GLSLCommenter : CodeDocumentationAwareCommenter {
    override fun isDocumentationComment(element: PsiComment?): Boolean = false

    override fun getDocumentationCommentTokenType(): IElementType? = null

    override fun getLineCommentTokenType(): IElementType? = GLSLTypes.COMMENT_LINE

    override fun getBlockCommentTokenType(): IElementType? = GLSLTypes.COMMENT_BLOCK

    override fun getDocumentationCommentLinePrefix(): String? = null

    override fun getDocumentationCommentPrefix(): String? = null

    override fun getDocumentationCommentSuffix(): String? = null

    override fun getCommentedBlockCommentPrefix(): String? = null

    override fun getCommentedBlockCommentSuffix(): String? = null

    override fun getBlockCommentPrefix(): String? = "/*"

    override fun getBlockCommentSuffix(): String? = "*/"

    override fun getLineCommentPrefix(): String? = "//"

}