package pw.binom.material.psi

import pw.binom.checkNext
import pw.binom.ifType
import pw.binom.skipSpace

class StatementBlock(val statements: List<Statement>, override val position: Int, override val length: Int) : Statement {
    companion object {
        fun read(lexer: LexStream<TokenType>): StatementBlock? =
                lexer.safe {
                    val startBlock = lexer.skipSpace(true)
                    startBlock?.ifType(TokenType.LEFT_BRACE)
                            ?: return@safe null

                    val list = ArrayList<Statement>()
                    var endBlock: LexStream.Element<TokenType>? = null
                    while (true) {
                        if (lexer.checkNext(true)?.element == TokenType.RIGHT_BRACE) {
                            endBlock = lexer.skipSpace(true)!!
                            break
                        }
                        val l = Statement.read(lexer) ?: break
                        list += l
                    }
                    endBlock ?: return@safe null
                    StatementBlock(list, startBlock.position, endBlock.position + 1 - startBlock.position)
                }
    }
}