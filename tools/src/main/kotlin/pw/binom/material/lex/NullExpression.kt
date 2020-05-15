package pw.binom.material.lex

import pw.binom.ifType
import pw.binom.material.SourcePoint
import pw.binom.skipSpace

class NullExpression(override val source: SourcePoint) : SimpleExpression {
    companion object {
        fun read(lexer: LexStream<TokenType>): NullExpression? =
                lexer.safe {
                    val num = lexer.skipSpace(true)
                            ?.ifType(TokenType.NULL)
                            ?: return@safe null
                    NullExpression(num.source())
                }
    }
}