package pw.binom.material.lex

import pw.binom.material.SourcePoint

interface Expression {
    val source:SourcePoint

    companion object {
        fun read(lexer: LexStream<TokenType>): Expression? = run {
            CommentDef.read(lexer)
            OperationExpression.read(lexer)
                    ?: UnitExpression.read(lexer)
        }
    }
}