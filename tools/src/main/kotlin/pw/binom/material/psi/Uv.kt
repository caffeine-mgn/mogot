package pw.binom.material.psi

import pw.binom.ifType
import pw.binom.skipSpace

class Uv(override val position: Int, override val length: Int) : Global {
    companion object {
        fun read(lexer: LexStream<TokenType>): Uv? = lexer.safe {
            val uv = lexer.skipSpace(true)?.ifType(TokenType.UV) ?: return@safe null
            if (lexer.skipSpace()?.element != TokenType.END_LINE)
                return@safe null
            Uv(uv.position, uv.text.length)
        }
    }
}