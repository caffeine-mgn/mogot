package pw.binom.glsl

import com.intellij.formatting.FormattingModel
import com.intellij.formatting.FormattingModelBuilder
import com.intellij.formatting.FormattingModelProvider
import com.intellij.formatting.SpacingBuilder
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CommonCodeStyleSettings
import pw.binom.glsl.psi.GLSLTypes
import pw.binom.material.MaterialLanguage


class GLSLFormattingModelBuilder : FormattingModelBuilder {
    override fun createModel(element: PsiElement, settings: CodeStyleSettings): FormattingModel {
        return FormattingModelProvider.createFormattingModelForPsiFile(
                element.containingFile,
                GLSLFormattingBlock(element.node, null, null, settings, createSpacingBuilder(settings)),
                settings
        )
    }

    private fun createSpacingBuilder(codeStyleSettings: CodeStyleSettings): SpacingBuilder {
        val commonSettings: CommonCodeStyleSettings? = codeStyleSettings.getCommonSettings(MaterialLanguage)
        return SpacingBuilder(codeStyleSettings, MaterialLanguage)
//                .before(COMMA).spaceIf(true/*commonSettings.SPACE_BEFORE_COMMA*/)
                .after(GLSLTypes.COMMA).spaces(1)
                .between(GLSLTypes.RIGHT_PARENTHESIS, GLSLTypes.BLOCK_STATEMENT).spaces(1)
                .after(GLSLTypes.RETURN_TYPE).spaces(1)
                .between(GLSLTypes.IF, GLSLTypes.LEFT_PARENTHESIS).spaces(1)
                .around(GLSLTypes.OP_EQ).spaces(1)
                .between(GLSLTypes.RIGHT_PARENTHESIS, GLSLTypes.STATEMENT).spaces(1)
                .between(GLSLTypes.RIGHT_PARENTHESIS, GLSLTypes.BLOCK_STATEMENT).spaces(1)
//                .before(GLSLTypes.LEFT_BRACE).spaces(1)
//                .after(GLSLTypes.LEFT_BRACE).none()
//                .before(GLSLTypes.RIGHT_BRACE).none()
//                .after(GLSLTypes.LEFT_BRACE).spaces(1)
//                .before(GLSLTypes.RIGHT_BRACE).spaces(1)

    }

    override fun getRangeAffectingIndent(file: PsiFile?, offset: Int, elementAtOffset: ASTNode?): TextRange? =
            elementAtOffset?.textRange
}