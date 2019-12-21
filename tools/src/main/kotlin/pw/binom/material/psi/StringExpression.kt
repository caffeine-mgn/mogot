package pw.binom.material.psi

import pw.binom.ifType
import pw.binom.skipSpace

class StringExpression(val text: String, val string: String, override val position: Int, override val length: Int) : SimpleExpression {
    companion object {
        fun read(lexStream: LexStream<TokenType>) = lexStream.safe {
            val element = lexStream.skipSpace(true)
                    ?.ifType(TokenType.STRING)
                    ?.let { it as Parser.StringElement }
                    ?: return@safe null
            StringExpression(element.text, element.string, element.position, element.text.length)
        }
    }
}