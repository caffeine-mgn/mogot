package pw.binom.material.psi

import pw.binom.checkNext
import pw.binom.skipSpace

class SubjectExpression(override val position: Int, override val length: Int) : SimpleExpression {
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