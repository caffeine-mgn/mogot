package pw.binom.material.lex

import pw.binom.ifType
import pw.binom.material.SourcePoint
import pw.binom.skipSpace

class IdAccessExpression(val exp: Expression?, val id: String, override val source: SourcePoint) : AssignebleExpression {
    companion object {
        fun read(exp: Expression?, lexer: LexStream<TokenType>): IdAccessExpression? =
                lexer.safe {
                    val id = lexer.skipSpace(true)
                            ?.ifType(TokenType.ID)
                            ?: return@safe null
                    IdAccessExpression(exp, id.text, id.source())
                }
    }
}