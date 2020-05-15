package pw.binom.material.lex

import pw.binom.material.SourcePoint
import pw.binom.skipSpace

class AssignStatement(
        val subject: AssignebleExpression,
        val exp: Expression,
        val operator: OperationExpression.Operator?,
        override val source: SourcePoint) : UnitStatement {

    companion object {
        private fun TokenType.fromAssign() = when (this) {
            TokenType.ASSIGN_DIV -> OperationExpression.Operator.DIV
            TokenType.ASSIGN_MINUS -> OperationExpression.Operator.MINUS
            TokenType.ASSIGN_PLUS -> OperationExpression.Operator.PLUS
            TokenType.ASSIGN_TIMES -> OperationExpression.Operator.TIMES
            TokenType.ASSIGN -> null
            else -> throw IllegalArgumentException("Unknown operator $this")
        }

        fun read(lexer: LexStream<TokenType>): AssignStatement? =
                lexer.safe {
                    val left = UnitExpression.read(lexer) ?: return@safe null
                    if (left !is AssignebleExpression) return@safe null
                    val eq = lexer.skipSpace(true)
                            ?.takeIf { it.element.isAssign }
                            ?: return@safe null
                    val right = Expression.read(lexer) ?: return@safe null
                    AssignStatement(left, right, eq.element.fromAssign(),
                            SourcePoint(lexer.module,
                                    left.source.position,
                                    right.source.position + right.source.length - left.source.position
                            )
                    )
                }
    }
}