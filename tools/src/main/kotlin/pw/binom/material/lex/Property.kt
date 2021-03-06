package pw.binom.material.lex

import pw.binom.checkNext
import pw.binom.ifType
import pw.binom.material.SourcePoint
import pw.binom.skipSpace

class Property(val properties: Map<String, String>, override val source: SourcePoint) : Global {
    companion object {
        fun read(lexer: LexStream<TokenType>): Property? = lexer.safe {
            val property = lexer.skipSpace(true)?.ifType(TokenType.PROPERTY) ?: return@safe null
            val properties = HashMap<String, String>()
            var end: LexStream.Element<TokenType>? = null
            if (lexer.checkNext()?.element == TokenType.LEFT_PARENTHESIS) {
                lexer.skipSpace()
                while (true) {
                    if (lexer.checkNext()?.element == TokenType.RIGHT_PARENTHESIS) {
                        end = lexer.skipSpace()
                        break
                    }
                    if (properties.isNotEmpty() && lexer.skipSpace()?.element != TokenType.COMMA)
                        return@safe null
                    val key = (IdAccessExpression.read(null, lexer) ?: return@safe null).id
                    if (lexer.skipSpace()?.element != TokenType.ASSIGN)
                        return@safe null
                    val value = (
                            NumberExpression.read(lexer)?.value
                                    ?: BooleanExpression.read(lexer)?.value?.let { if (it) "true" else "false" }
                                    ?: StringExpression.read(lexer)?.string
                                    ?: return@safe null)
                    properties[key] = value
                }
                if (lexer.skipSpace()?.element != TokenType.END_LINE)
                    return@safe null
            }
            Property(
                    properties,
                    SourcePoint(
                            lexer.module,
                            property.position,
                            end?.position?.let { it + 1 - property.position }
                                    ?: property.text.length
                    )
            )
        }
    }
}