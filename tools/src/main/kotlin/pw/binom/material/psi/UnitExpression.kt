package pw.binom.material.psi

interface UnitExpression {
    companion object {
        fun read(lexStream: LexStream<TokenType>):SimpleExpression? =
                IncDecExpression.read(lexStream)
                        ?: SubjectExpression.read(lexStream)
    }
}