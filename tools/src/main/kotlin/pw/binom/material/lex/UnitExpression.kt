package pw.binom.material.lex

interface UnitExpression {
    companion object {
        fun read(lexStream: LexStream<TokenType>):SimpleExpression? =
                IncDecExpression.read(lexStream)
                        ?: SubjectExpression.read(lexStream)
    }
}