package pw.binom.material.psi

import pw.binom.ifType
import pw.binom.skipSpace

class Model(override val position: Int, override val length: Int) : Global {
    companion object {
        fun read(lexer: LexStream<TokenType>): Model? = lexer.safe {
            val model = lexer.skipSpace(true)?.ifType(TokenType.MODEL) ?: return@safe null
            if (lexer.skipSpace()?.element != TokenType.END_LINE)
                return@safe null
            Model(model.position, model.text.length)
        }
    }
}