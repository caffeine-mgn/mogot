package pw.binom.material.lex

/*
class Annotation(val name: String, val properties: Map<String, String>, override val position: Int, override val length: Int, val line: Int, val column: Int, val text: String) : Global {
    companion object {
        fun read(lexer: LexStream<TokenType>): Annotation? = lexer.safe {
            val model = lexer.skipSpace(true)?.ifType(TokenType.ANNOTATION) ?: return@safe null
            val name = model.text.removePrefix("@")
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
            Annotation(name, properties, model.position, model.text.length, line = model.line, column = model.column, text = model.text)
        }
    }
}
*/