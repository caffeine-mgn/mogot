package pw.binom.material.psi

import pw.binom.ifType
import pw.binom.skipSpace

class Vertex(override val position: Int, override val length: Int) : Global {
    companion object {
        fun read(lexer: LexStream<TokenType>): Vertex? = lexer.safe {
            val vertex = lexer.skipSpace(true)
                    ?.ifType(TokenType.VERTEX) ?: return@safe null
            if (lexer.skipSpace()?.element != TokenType.END_LINE)
                return@safe null
            Vertex(vertex.position, vertex.text.length)
        }
    }
}