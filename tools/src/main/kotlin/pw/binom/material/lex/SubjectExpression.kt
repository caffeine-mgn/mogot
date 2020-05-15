package pw.binom.material.lex

import pw.binom.checkNext
import pw.binom.material.SourcePoint
import pw.binom.skipSpace

class SubjectExpression(override val source: SourcePoint) : SimpleExpression {
    companion object {
        fun read(lexer: LexStream<TokenType>): SimpleExpression? =
                lexer.safe {
                    var l = SimpleExpression.read(null, lexer) ?: return@safe null
                    while (true) {
                        val ar = ArrayAccessExpression.read(l, lexer)
                        if (ar != null) {
                            l = ar
                            continue
                        }
                        if (lexer.checkNext(true)?.element != TokenType.DOT)
                            break
                        lexer.skipSpace(true)
                        l = SimpleExpression.read(l, lexer) ?: return@safe null
                    }
                    return@safe l
                }
    }
}