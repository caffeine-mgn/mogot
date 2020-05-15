package pw.binom.material.lex

import pw.binom.material.SourcePoint

class ExpStatement(val exp: Expression, override val source: SourcePoint) : UnitStatement {
    companion object {
        fun read(lexer: LexStream<TokenType>): ExpStatement? =
                lexer.safe {
                    val exp = Expression.read(lexer) ?: return@safe null
                    ExpStatement(exp, exp.source)
                }
    }
}