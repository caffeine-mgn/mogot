package pw.binom.material.lex

import pw.binom.checkNext
import pw.binom.ifType
import pw.binom.material.SourcePoint
import pw.binom.skipSpace

class MethodCallExpression(
        val exp: Expression?,
        val method: String,
        val args: List<Expression>,
        override val source: SourcePoint) : SimpleExpression {
    companion object {
        fun read(exp: Expression?, lexer: LexStream<TokenType>): MethodCallExpression? =
                lexer.safe {
                    val id = lexer.skipSpace(true)
                            ?.takeIf { it.element.isPrimitive || it.element == TokenType.ID }
                            ?: return@safe null
                    lexer.skipSpace(false)
                            ?.ifType(TokenType.LEFT_PARENTHESIS)
                            ?: return@safe null

                    val list = ArrayList<Expression>()

                    var end: LexStream.Element<TokenType>? = null
                    while (true) {
                        if (lexer.checkNext(true)?.element == TokenType.RIGHT_PARENTHESIS) {
                            end = lexer.skipSpace(true)
                            break
                        }
                        if (list.isNotEmpty()) {
                            lexer.skipSpace(true)
                                    ?.ifType(TokenType.COMMA)
                                    ?: return@safe null
                        }
                        list += Expression.read(lexer) ?: return@safe null
                    }
                    MethodCallExpression(exp, id.text, list,
                            SourcePoint(
                                    lexer.module,
                                    id.position,
                                    end!!.position + 1 - id.position
                            )
                    )
                }
    }
}