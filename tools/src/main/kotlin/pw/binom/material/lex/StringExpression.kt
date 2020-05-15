package pw.binom.material.lex

import pw.binom.ifType
import pw.binom.material.SourcePoint
import pw.binom.skipSpace

class StringExpression(val text: String, val string: String, override val source: SourcePoint) : SimpleExpression {
    companion object {
        fun read(lexStream: LexStream<TokenType>) = lexStream.safe {
            val element = lexStream.skipSpace(true)
                    ?.ifType(TokenType.STRING)
                    ?.let { it as Parser.StringElement }
                    ?: return@safe null
            StringExpression(element.text, element.string, element.source())
        }
    }
}