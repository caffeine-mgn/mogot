package pw.binom.material.psi

import pw.binom.skipSpace

class BooleanExpression(val value: Boolean, override val position: Int, override val length: Int) : SimpleExpression {
    companion object {
        fun read(lexStream: LexStream<TokenType>) = lexStream.safe {
            val e = lexStream.skipSpace(true) ?: return@safe null
            val value = when (e.element) {
                TokenType.TRUE -> true
                TokenType.FALSE -> false
                else -> return@safe null
            }
            BooleanExpression(value, e.position, e.text.length)
        }
    }
}