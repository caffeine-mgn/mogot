package pw.binom.material.lex

import pw.binom.material.SourcePoint
import pw.binom.skipSpace

class BooleanExpression(val value: Boolean, override val source: SourcePoint) : SimpleExpression {
    companion object {
        fun read(lexStream: LexStream<TokenType>) = lexStream.safe {
            val e = lexStream.skipSpace(true) ?: return@safe null
            val value = when (e.element) {
                TokenType.TRUE -> true
                TokenType.FALSE -> false
                else -> return@safe null
            }
            BooleanExpression(value, e.source())
        }
    }
}