package pw.binom.material.psi

import pw.binom.ifType
import pw.binom.skipSpace

class InvertExpression(val exp: SimpleExpression, override val position: Int, override val length: Int) : SimpleExpression {
    companion object {
        fun read(lexStream: LexStream<TokenType>): InvertExpression? = lexStream.safe {
            val first = lexStream.skipSpace(true)?.ifType(TokenType.OP_MINUS) ?: return@safe null
            val exp = UnitExpression.read(lexStream) ?: return@safe null
            return@safe InvertExpression(
                    exp,
                    first.position,
                    exp.position + exp.length - first.position
            )
        }
    }
}