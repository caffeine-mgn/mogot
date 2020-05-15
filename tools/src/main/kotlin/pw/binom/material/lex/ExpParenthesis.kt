package pw.binom.material.lex

import pw.binom.ifType
import pw.binom.material.SourcePoint
import pw.binom.skipSpace

class ExpParenthesis(val exp: Expression, override val source: SourcePoint) : SimpleExpression {
    companion object {
        fun read(lexStream: LexStream<TokenType>) = lexStream.safe {
            val first = lexStream.skipSpace(true)?.ifType(TokenType.LEFT_PARENTHESIS) ?: return@safe null
            val exp = Expression.read(lexStream) ?: return@safe null
            val second = lexStream.skipSpace(true)?.ifType(TokenType.RIGHT_PARENTHESIS) ?: return@safe null
            ExpParenthesis(
                    exp,
                    SourcePoint(
                            lexStream.module,
                            first.position,
                            second.position + second.text.length - first.position
                    )
            )
        }
    }
}