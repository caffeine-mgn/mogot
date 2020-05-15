package pw.binom.material.lex

import pw.binom.checkNext
import pw.binom.ifType
import pw.binom.material.SourcePoint
import pw.binom.skipSpace

class AnnotationExp(val type: TypeId,
                    val body: Map<String, Expression>,
                    override val source: SourcePoint) : Global {
    companion object {
        fun read(lexer: LexStream<TokenType>): AnnotationExp? = lexer.safe {
            val model = lexer.skipSpace(true)?.ifType(TokenType.ANNOTATION) ?: return@safe null
            val name = model.text.removePrefix("@")
            val type = TypeId(name, emptyList(), model.position, model.text.length)
            val properties = HashMap<String, Expression>()
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
                    val value = Expression.read(lexer) ?: return@safe null
//                    val value = (
//                            NumberExpression.read(lexer)?.value
//                                    ?: BooleanExpression.read(lexer)?.value?.let { if (it) "true" else "false" }
//                                    ?: StringExpression.read(lexer)?.string
//                                    ?: return@safe null)
                    properties[key] = value
                }
                if (lexer.skipSpace()?.element != TokenType.END_LINE)
                    return@safe null
            }
            AnnotationExp(
                    type = type,
                    body = properties,
                    source = model.source()
            )
        }
    }
}