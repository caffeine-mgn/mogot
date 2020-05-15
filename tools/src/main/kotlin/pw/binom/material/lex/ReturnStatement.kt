package pw.binom.material.lex

import pw.binom.material.SourcePoint
import pw.binom.skipSpace

class ReturnStatement(val exp: Expression?, override val source: SourcePoint) : Statement {
    companion object {
        fun read(lexer: LexStream<TokenType>): ReturnStatement? =
                lexer.safe {
                    val returnElement = lexer.skipSpace(skipEndLine = true)?.takeIf { it.element == TokenType.RETURN }
                            ?: return@safe null

                    val exp = Expression.read(lexer)
                    val end = if (exp != null)
                        exp.source.position + exp.source.length
                    else
                        returnElement.position + returnElement.text.length
                    return@safe ReturnStatement(exp,
                            SourcePoint(
                                    lexer.module,
                                    returnElement.position,
                                    end - returnElement.position
                            )
                    )
                }
    }
}