package pw.binom.material.lex

import pw.binom.ifType
import pw.binom.material.SourcePoint
import pw.binom.skipSpace

class IfStatement(val condition: Expression,
                  val thenBlock: Statement,
                  val elseBlock: Statement?,
                  override val source: SourcePoint) : Statement {
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
                    SourcePoint(
                            lex.module,
                            first.position,
                            end.source.position + end.source.length - first.position
                    )
            )
        }
    }
}