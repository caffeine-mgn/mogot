package pw.binom.material.psi

import pw.binom.checkNext
import pw.binom.skipSpace

class IncDecExpression(
        override val position: Int,
        override val length: Int,
        val operator: OperationExpression.Operator,
        val exp: SimpleExpression,
        val prefix: Boolean
) : SimpleExpression {
    companion object {
        fun read(lexStream: LexStream<TokenType>) = lexStream.safe {
            val first = lexStream.checkNext(true) ?: return@safe null
            if (first.element == TokenType.INC || first.element == TokenType.DEC) {
                lexStream.skipSpace(true)
                val operator = when (first.element) {
                    TokenType.INC -> OperationExpression.Operator.PLUS
                    TokenType.DEC -> OperationExpression.Operator.MINUS
                    else -> return@safe null
                }

                val exp = SubjectExpression.read(lexStream) ?: return@safe null
                return@safe IncDecExpression(
                        first.position,
                        exp.position + exp.length - first.position,
                        operator,
                        exp,
                        true
                )
            }
            val vv = SubjectExpression.read(lexStream) ?: return@safe null
            val el = lexStream.skipSpace(true) ?: return@safe null
            val operator = when (el.element) {
                TokenType.INC -> OperationExpression.Operator.PLUS
                TokenType.DEC -> OperationExpression.Operator.MINUS
                else -> return@safe null
            }

            return@safe IncDecExpression(
                    vv.position,
                    el.position + el.text.length - vv.position,
                    operator,
                    vv,
                    false
            )
        }
    }
}