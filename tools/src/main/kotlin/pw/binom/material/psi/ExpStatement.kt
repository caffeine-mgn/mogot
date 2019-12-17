package pw.binom.material.psi

import pw.binom.ifType
import pw.binom.skipSpace

class ExpStatement(val exp: Expression, override val position: Int, override val length: Int) : Statement {
    companion object {
        fun read(lexer: LexStream<TokenType>): ExpStatement? =
                lexer.safe {
                    val exp = Expression.read(lexer) ?: return@safe null
                    lexer.skipSpace()?.ifType(TokenType.END_LINE) ?: return@safe null
                    ExpStatement(exp, exp.position, exp.length)
                }
    }
}