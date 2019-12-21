package pw.binom.material.psi

import pw.binom.ifType
import pw.binom.skipSpace

class IdAccessExpression(val exp: Expression?, val id: String, override val position: Int, override val length: Int) : AssignebleExpression {
    companion object {
        fun read(exp: Expression?, lexer: LexStream<TokenType>): IdAccessExpression? =
                lexer.safe {
                    val id = lexer.skipSpace(true)
                            ?.ifType(TokenType.ID)
                            ?: return@safe null
                    IdAccessExpression(exp, id.text, id.position, id.text.length)
                }
    }
}