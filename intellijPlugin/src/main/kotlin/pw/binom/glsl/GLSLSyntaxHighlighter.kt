package pw.binom.glsl

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import pw.binom.glsl.psi.GLSLTypes

class GLSLSyntaxHighlighter : SyntaxHighlighterBase() {
    companion object {
        val SEPARATOR = createTextAttributesKey("SIMPLE_SEPARATOR", DefaultLanguageHighlighterColors.OPERATION_SIGN)
        val KEY = createTextAttributesKey("SIMPLE_KEY", DefaultLanguageHighlighterColors.KEYWORD)
        val ID = createTextAttributesKey("ID", DefaultLanguageHighlighterColors.IDENTIFIER)
        val NUMBER = createTextAttributesKey("SIMPLE_NUMBER", DefaultLanguageHighlighterColors.NUMBER)
        val VALUE = createTextAttributesKey("SIMPLE_VALUE", DefaultLanguageHighlighterColors.STRING)
        val COMMENT = createTextAttributesKey("SIMPLE_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT)
        val PARENTHESES = createTextAttributesKey("PARENTHESES", DefaultLanguageHighlighterColors.PARENTHESES)
        val BRACES = createTextAttributesKey("BRACES", DefaultLanguageHighlighterColors.BRACES)
        val BAD_CHARACTER = createTextAttributesKey("SIMPLE_BAD_CHARACTER", HighlighterColors.BAD_CHARACTER)

        private val BAD_CHAR_KEYS = arrayOf(BAD_CHARACTER)
        private val SEPARATOR_KEYS = arrayOf(SEPARATOR)
        private val KEY_KEYS = arrayOf(KEY)
        private val ID_KEYS = arrayOf(ID)
        private val NUMBER_KEYS = arrayOf(NUMBER)
        private val VALUE_KEYS = arrayOf(VALUE)
        private val COMMENT_KEYS = arrayOf(COMMENT)
        private val PARENTHESES_KEYS = arrayOf(PARENTHESES)
        private val BRACES_KEYS = arrayOf(BRACES)
        private val EMPTY_KEYS = Array<TextAttributesKey>(0) { TODO() }
    }

    override fun getTokenHighlights(tokenType: IElementType?): Array<TextAttributesKey> =
            when (tokenType) {
                GLSLTypes.NUMBER -> NUMBER_KEYS
                TokenType.BAD_CHARACTER -> BAD_CHAR_KEYS
                GLSLTypes.PRIMITIVE,
                GLSLTypes.VEC3,
                GLSLTypes.VEC4,
                GLSLTypes.INT,
                GLSLTypes.FLOAT,
                GLSLTypes.UNIFORM,
                GLSLTypes.IN,
                GLSLTypes.OUT,
                GLSLTypes.CONVER_PRIMITIVE,
                GLSLTypes.VOID,
                GLSLTypes.RETURN,
                GLSLTypes.FOR,
                GLSLTypes.STRUCT -> KEY_KEYS

                GLSLTypes.ID -> ID_KEYS

                GLSLTypes.COMMENT_LINE,
                GLSLTypes.COMMENT_BLOCK,
                GLSLTypes.DERECTIVE
                -> COMMENT_KEYS

                GLSLTypes.RIGHT_PARENTHESIS,
                GLSLTypes.LEFT_PARENTHESIS -> PARENTHESES_KEYS

                GLSLTypes.RIGHT_BRACE,
                GLSLTypes.LEFT_BRACE -> BRACES_KEYS

                else -> EMPTY_KEYS
            }

    override fun getHighlightingLexer(): Lexer = GLSLLexerAdapter()

}