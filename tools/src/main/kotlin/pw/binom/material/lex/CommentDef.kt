package pw.binom.material.lex

import pw.binom.checkNext
import pw.binom.skipSpace

class CommentDef {
    companion object {
        fun read(lexer: LexStream<TokenType>): Global? {
            while (true) {
                val vv = lexer.checkNext(true)
                if (vv?.element == TokenType.COMMENT_LINE) {
                    lexer.skipSpace(true)
                    continue
                }

                if (vv?.element == TokenType.COMMENT_BLOCK) {
                    lexer.skipSpace(true)
                    continue
                }
                break
            }
            return null
        }
    }
}