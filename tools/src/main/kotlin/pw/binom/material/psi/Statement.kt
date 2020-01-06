package pw.binom.material.psi

interface Statement {
    val position: Int
    val length: Int

    companion object {
        fun read(lexer: LexStream<TokenType>): Statement? = run {
            CommentDef.read(lexer)
            StatementBlock.read(lexer)
                    ?: UnitStatement.read(lexer)
                    ?: ReturnStatement.read(lexer)
                    ?: ForStatement.read(lexer)
        }
    }
}