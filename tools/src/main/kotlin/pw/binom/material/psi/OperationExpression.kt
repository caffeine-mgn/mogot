package pw.binom.material.psi

import pw.binom.skipSpace

class OperationExpression(
        val operator: Operator,
        val left: Expression,
        val right: Expression,
        override val position: Int,
        override val length: Int) : Expression {
    enum class Operator(val innerMethod: String, val assignMethod: String) {
        DIV("div", "assignDiv"),
        TIMES("times", "assignTimes"),
        PLUS("plus", "assignPlus"),
        MINUS("minus", "assignMinus"),
        GT("compareTo", "compareTo"),
        GE("compareTo", "compareTo"),
        LT("compareTo", "compareTo"),
        LE("compareTo", "compareTo");

        companion object {
            fun fromToken(token: TokenType) =
                    when (token) {
                        TokenType.OP_DIV -> DIV
                        TokenType.OP_TIMES -> TIMES
                        TokenType.OP_PLUS -> PLUS
                        TokenType.OP_MINUS -> PLUS
                        TokenType.OP_GT -> GT
                        TokenType.OP_GE -> GE
                        TokenType.OP_LT -> LT
                        TokenType.OP_LE -> LE
                        else -> throw IllegalArgumentException("Unknown operator $token")
                    }
        }
    }

    companion object {
        fun read(lexer: LexStream<TokenType>): OperationExpression? =
                lexer.safe {
                    val left = SubjectExpression.read(lexer) ?: return@safe null
                    val operator = lexer.skipSpace(true)
                            ?.takeIf { it.element.isOperation }
                            ?: return@safe null
                    val right = Expression.read(lexer) ?: return@safe null
                    OperationExpression(
                            Operator.fromToken(operator.element),
                            left,
                            right,
                            left.position, right.position + right.length - left.position)
                }
    }
}