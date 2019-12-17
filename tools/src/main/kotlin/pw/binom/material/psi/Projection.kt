package pw.binom.material.psi

import pw.binom.ifType
import pw.binom.skipSpace

class Projection(override val position: Int, override val length: Int) : Global {
    companion object {
        fun read(lexer: LexStream<TokenType>): Projection? = lexer.safe {
            val projection = lexer.skipSpace(true)?.ifType(TokenType.PROJECTION) ?: return@safe null
            if (lexer.skipSpace()?.element != TokenType.END_LINE)
                return@safe null
            Projection(projection.position, projection.text.length)
        }
    }
}