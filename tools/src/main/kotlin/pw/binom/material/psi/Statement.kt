package pw.binom.material.psi

interface Statement {
    val position: Int
    val length: Int

    companion object {
        fun read(lexer: LexStream<TokenType>): Statement? =
                StatementBlock.read(lexer)
                        ?: LocalDefineAssignStatement.read(lexer)
                        ?: AssignStatement.read(lexer)
                        ?: ReturnStatement.read(lexer)
                        ?: ExpStatement.read(lexer)
                        ?: ForStatement.read(lexer)
    }
}