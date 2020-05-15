package pw.binom.material.lex

interface UnitStatement:Statement{
    companion object {
        fun read(lexer: LexStream<TokenType>): UnitStatement? =
                LocalDefineAssignStatement.read(lexer)
                        ?: AssignStatement.read(lexer)
                        ?: ExpStatement.read(lexer)
    }
}