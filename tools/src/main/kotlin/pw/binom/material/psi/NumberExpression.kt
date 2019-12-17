package pw.binom.material.psi

import pw.binom.ifType
import pw.binom.skipSpace

class NumberExpression(val value: String, override val position: Int, override val length: Int) : SimpleExpression {
    companion object {
        fun read(lexer: LexStream<TokenType>): NumberExpression? =
                lexer.safe {
                    val num = lexer.skipSpace(true)
                            ?.ifType(TokenType.NUMBER)
                            ?: return@safe null
                    NumberExpression(num.text, num.position, num.text.length)
                }
    }
}