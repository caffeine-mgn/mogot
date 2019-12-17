package pw.binom.glsl

import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.psi.PsiElement
import com.intellij.lang.ParserDefinition.SpaceRequirements
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IFileElementType
import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.TokenType
import com.intellij.psi.tree.TokenSet
import pw.binom.material.MaterialLanguage
import pw.binom.glsl.psi.GLSLFile
import pw.binom.glsl.psi.GLSLTypes


class GLSLParserDefinition : ParserDefinition {
    val WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE)
    val COMMENTS = TokenSet.create(GLSLTypes.COMMENT_LINE, GLSLTypes.COMMENT_BLOCK)

    val FILE = IFileElementType(MaterialLanguage)

    override fun createLexer(project: Project): Lexer {
        return GLSLLexerAdapter()
    }

    override fun getWhitespaceTokens(): TokenSet {
        return WHITE_SPACES
    }

    override fun getCommentTokens(): TokenSet {
        return COMMENTS
    }

    override fun getStringLiteralElements(): TokenSet {
        return TokenSet.EMPTY
    }

    override fun createParser(project: Project): PsiParser {
        return GLSLParser()
    }

    override fun getFileNodeType(): IFileElementType {
        return FILE
    }

    override fun createFile(viewProvider: FileViewProvider): PsiFile {
        return GLSLFile(viewProvider)
    }

    override fun spaceExistenceTypeBetweenTokens(left: ASTNode, right: ASTNode): SpaceRequirements {
        return SpaceRequirements.MAY
    }

    override fun createElement(node: ASTNode): PsiElement {
        return GLSLTypes.Factory.createElement(node)
    }
}