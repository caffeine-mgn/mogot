package pw.binom.material.lex

import pw.binom.checkNext
import pw.binom.material.SourcePoint
import pw.binom.skipSpace

class IncDecExpression(
        val operator: OperationExpression.Operator,
        val exp: SimpleExpression,
        val prefix: Boolean,
        override val source: SourcePoint
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
                        operator,
                        exp,
                        true,
                        SourcePoint(
                                lexStream.module,
                                first.position,
                                exp.source.position + exp.source.length - first.position
                        )
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
                    operator,
                    vv,
                    false,
                    SourcePoint(
                            lexStream.module,
                            vv.source.position,
                            el.position + el.text.length - vv.source.position
                    )
            )
        }
    }
}