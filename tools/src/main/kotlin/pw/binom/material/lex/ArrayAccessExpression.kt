package pw.binom.material.lex

import pw.binom.ifType
import pw.binom.material.SourcePoint
import pw.binom.skipSpace

class ArrayAccessExpression(val exp: Expression, val index: Expression, override val source: SourcePoint) : SimpleExpression {
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
                    ArrayAccessExpression(
                            exp,
                            index,
                            SourcePoint(
                                    lexer.module,
                                    start.position,
                                    end.position + 1 - start.position
                            )
                    )
                }
    }
}