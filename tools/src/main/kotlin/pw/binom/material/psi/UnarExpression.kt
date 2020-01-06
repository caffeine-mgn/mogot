package pw.binom.material.psi

import pw.binom.checkNext
import pw.binom.skipSpace

class UnarExpression(
        override val position: Int,
        override val length: Int,
        val operator: OperationExpression.Operator,
        val exp: Expression,
        val prefix: Boolean
) : SimpleExpression {
    companion object {
        private var parsing = false
        fun read(lexStream: LexStream<TokenType>) = lexStream.safe {
            if (parsing)
                return@safe null
            parsing = true
            try {
                val first = lexStream.checkNext(true) ?: return@safe null
                if (first.element == TokenType.UNARY_PLUS || first.element == TokenType.UNARY_MINUS) {
                    lexStream.skipSpace(true)
                    val operator = when (first.element) {
                        TokenType.UNARY_PLUS -> OperationExpression.Operator.PLUS
                        TokenType.UNARY_MINUS -> OperationExpression.Operator.MINUS
                        else -> return@safe null
                    }

                    val exp = SubjectExpression.read(lexStream) ?: return@safe null
                    return@safe UnarExpression(
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
                    TokenType.UNARY_PLUS -> OperationExpression.Operator.PLUS
                    TokenType.UNARY_MINUS -> OperationExpression.Operator.MINUS
                    else -> return@safe null
                }

                return@safe UnarExpression(
                        vv.position,
                        el.position + el.text.length - vv.position,
                        operator,
                        vv,
                        false
                )
            } finally {
                parsing = false
            }
        }
    }
}