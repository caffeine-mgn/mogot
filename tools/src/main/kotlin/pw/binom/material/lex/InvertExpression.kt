package pw.binom.material.lex

import pw.binom.ifType
import pw.binom.material.SourcePoint
import pw.binom.skipSpace

class InvertExpression(val exp: SimpleExpression, override val source: SourcePoint) : SimpleExpression {
    companion object {
        fun read(lexStream: LexStream<TokenType>): InvertExpression? = lexStream.safe {
            val first = lexStream.skipSpace(true)?.ifType(TokenType.OP_MINUS) ?: return@safe null
            val exp = UnitExpression.read(lexStream) ?: return@safe null
            return@safe InvertExpression(
                    exp,
                    SourcePoint(
                            lexStream.module,
                            first.position,
                            exp.source.position + exp.source.length - first.position
                    )
            )
        }
    }
}