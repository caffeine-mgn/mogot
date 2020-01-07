package pw.binom.material.psi

import pw.binom.ifType
import pw.binom.skipSpace

class ForStatement(
        val init: Statement?,
        val end: Expression?,
        val step: Statement?,
        val statement: Statement,
        override val position: Int,
        override val length: Int) : Statement {
    companion object {
        fun read(lex: LexStream<TokenType>) = lex.safe {
            val start = lex.skipSpace(true)?.ifType(TokenType.FOR) ?: return@safe null
            lex.skipSpace(true)?.ifType(TokenType.LEFT_PARENTHESIS) ?: return@safe null
            val init = UnitStatement.read(lex)
            lex.skipSpace(false)?.ifType(TokenType.CMD_SEPARATOR) ?: return@safe null
            val end = Expression.read(lex)
            val r = lex.skipSpace(false)
                    r?.ifType(TokenType.CMD_SEPARATOR) ?: return@safe null
            val step = UnitStatement.read(lex)
            lex.skipSpace(true)?.ifType(TokenType.RIGHT_PARENTHESIS) ?: return@safe null
            val statement = Statement.read(lex) ?: return@safe null

            return@safe ForStatement(
                    init = init,
                    end = end,
                    step = step,
                    statement = statement,
                    position = start.position,
                    length = statement.position + statement.length - start.position
            )
        }
    }
}