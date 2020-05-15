package pw.binom.material.lex

import pw.binom.ifType
import pw.binom.material.SourcePoint
import pw.binom.skipSpace

class NumberExpression(val value: String, override val source: SourcePoint) : SimpleExpression {
    companion object {
        fun read(lexer: LexStream<TokenType>): NumberExpression? =
                lexer.safe {
                    val num = lexer.skipSpace(true)
                            ?.ifType(TokenType.NUMBER)
                            ?: return@safe null
                    NumberExpression(num.text, num.source())
                }
    }
}