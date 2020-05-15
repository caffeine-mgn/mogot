package pw.binom.material.lex

import pw.binom.material.SourcePoint

interface Statement {
    val source:SourcePoint

    companion object {
        fun read(lexer: LexStream<TokenType>): Statement? = run {
            CommentDef.read(lexer)
            StatementBlock.read(lexer)
                    ?: IfStatement.read(lexer)
                    ?: UnitStatement.read(lexer)
                    ?: ReturnStatement.read(lexer)
                    ?: ForStatement.read(lexer)
        }
    }
}