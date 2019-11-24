package pw.binom.glsl

import com.intellij.lang.BracePair
import com.intellij.lang.PairedBraceMatcher
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import pw.binom.glsl.psi.GLSLTypes

class GLSLPairedBraceMatcher : PairedBraceMatcher {
    override fun getCodeConstructStart(file: PsiFile?, openingBraceOffset: Int): Int = openingBraceOffset

    private val pairs = arrayOf(
            BracePair(GLSLTypes.LEFT_BRACE, GLSLTypes.RIGHT_BRACE, true),
            BracePair(GLSLTypes.LEFT_PARENTHESIS, GLSLTypes.RIGHT_PARENTHESIS, true),
            BracePair(GLSLTypes.LEFT_INDEX, GLSLTypes.RIGHT_INDEX, true)
    )

    override fun getPairs(): Array<BracePair> = pairs

    override fun isPairedBracesAllowedBeforeType(lbraceType: IElementType, contextType: IElementType?): Boolean = true

}