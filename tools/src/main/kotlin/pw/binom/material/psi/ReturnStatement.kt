package pw.binom.material.psi

import pw.binom.skipSpace

class ReturnStatement(val exp: Expression?, override val position: Int, override val length: Int) : Statement {
    companion object {
        fun read(lexer: LexStream<TokenType>): ReturnStatement? =
                lexer.safe {
                    val returnElement = lexer.skipSpace(skipEndLine = true)?.takeIf { it.element == TokenType.RETURN }
                            ?: return@safe null

                    val exp = Expression.read(lexer)
                    val end = if (exp != null)
                        exp.position + exp.length
                    else
                        returnElement.position + returnElement.text.length
                    return@safe ReturnStatement(exp,
                            returnElement.position,
                            end - returnElement.position
                    )
                }
    }
}