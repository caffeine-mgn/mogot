package pw.binom.material.psi

import pw.binom.ifType
import pw.binom.skipSpace

class ArrayAccessExpression(val exp: Expression, val index: Expression, override val position: Int, override val length: Int) : SimpleExpression {
    companion object {
        fun read(exp: Expression, lexer: LexStream<TokenType>): ArrayAccessExpression? =
                lexer.safe {
                    val start = lexer.skipSpace(true)
                            ?.ifType(TokenType.LEFT_INDEX)
                            ?: return@safe null

                    val index = Expression.read(lexer) ?: return@safe null
                    val end = lexer.skipSpace(true)
                            ?.ifType(TokenType.RIGHT_INDEX)
                            ?: return@safe null
                    ArrayAccessExpression(exp, index, start.position, end.position + 1 - start.position)
                }
    }
}