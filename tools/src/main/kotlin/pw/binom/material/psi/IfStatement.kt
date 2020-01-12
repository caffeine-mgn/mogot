package pw.binom.material.psi

import pw.binom.ifType
import pw.binom.skipSpace

class IfStatement(val condition: Expression,
                  val thenBlock: Statement,
                  val elseBlock: Statement?,
                  override val position: Int,
                  override val length: Int) : Statement {
    companion object {
        fun read(lex: LexStream<TokenType>): IfStatement? = lex.safe {
            val first = lex.skipSpace(true)?.ifType(TokenType.IF) ?: return@safe null
            lex.skipSpace(true)?.ifType(TokenType.LEFT_PARENTHESIS) ?: return@safe null
            val condition = Expression.read(lex) ?: return@safe null
            lex.skipSpace(true)?.ifType(TokenType.RIGHT_PARENTHESIS) ?: return@safe null
            val thenBlock = Statement.read(lex) ?: return@safe null
            val elseBlock = if (lex.skipSpace(true)?.ifType(TokenType.ELSE) != null)
                Statement.read(lex)
            else
                null
            val end = elseBlock ?: thenBlock
            IfStatement(
                    condition, thenBlock, elseBlock,
                    first.position,
                    end.position + end.length - first.position
            )
        }
    }
}