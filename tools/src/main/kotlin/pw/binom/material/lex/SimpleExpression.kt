package pw.binom.material.lex

interface SimpleExpression : Expression {
    companion object {
        fun read(exp: Expression?, lexer: LexStream<TokenType>): SimpleExpression? =
                MethodCallExpression.read(exp, lexer)
                        ?: NullExpression.read(lexer)
                        ?: InvertExpression.read(lexer)
                        ?: BooleanExpression.read(lexer)
                        ?: NumberExpression.read(lexer)
                        ?: StringExpression.read(lexer)
                        ?: IdAccessExpression.read(exp, lexer)
                        ?: ExpParenthesis.read(lexer)
    }
}