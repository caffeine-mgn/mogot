package pw.binom.material.psi

import pw.binom.ifType
import pw.binom.skipSpace

class Normal(override val position: Int, override val length: Int) : Global {
    companion object {
        fun read(lexer: LexStream<TokenType>): Normal? = lexer.safe {
            val normal = lexer.skipSpace(true)?.ifType(TokenType.NORMAL) ?: return@safe null
            if (lexer.skipSpace()?.element != TokenType.END_LINE)
                return@safe null
            Normal(normal.position, normal.text.length)
        }
    }
}