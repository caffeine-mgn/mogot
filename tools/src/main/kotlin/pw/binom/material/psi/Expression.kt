package pw.binom.material.psi

interface Expression {
    val position: Int
    val length: Int

    companion object {
        fun read(lexer: LexStream<TokenType>): Expression? = run {
            CommentDef.read(lexer)
            OperationExpression.read(lexer)
                    ?: UnitExpression.read(lexer)
        }
    }
}