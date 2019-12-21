package pw.binom.material.psi

interface Expression {
    val position: Int
    val length: Int

    companion object {
        fun read(lexer: LexStream<TokenType>): Expression? =
                OperationExpression.read(lexer)
                        ?: SubjectExpression.read(lexer)
    }
}