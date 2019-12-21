package pw.binom.material.psi

import pw.binom.checkNext
import pw.binom.ifType
import pw.binom.skipSpace

class GlobalMethod(
        val returnType: Type,
        val name: String,
        val args: List<Argument>,
        val statement: StatementBlock,
        override val position: Int,
        override val length: Int) : Global {
    companion object {
        fun read(lexer: LexStream<TokenType>): GlobalMethod? =
                lexer.safe {
                    val classId = ClassId.read(lexer) ?: return@safe null
                    val type = ClassId.readArray(classId, lexer) ?: return@safe null
                    val methodName = lexer.skipSpace()
                            ?.takeIf { it.element.isPrimitive || it.element == TokenType.ID }
                            ?: return@safe null
                    lexer.skipSpace(true)
                            ?.ifType(TokenType.LEFT_PARENTHESIS)
                            ?: return@safe null
                    val args = ArrayList<Argument>()
                    while (true) {
                        if (lexer.checkNext(true)?.element == TokenType.RIGHT_PARENTHESIS) {
                            lexer.skipSpace(true)
                            break
                        }
                        if (args.isNotEmpty()) {
                            lexer.skipSpace(true)
                                    ?.ifType(TokenType.COMMA)
                                    ?: return@safe null
                        }
                        args += Argument.read(lexer) ?: return@safe null
                    }
                    val block = StatementBlock.read(lexer) ?: return@safe null
                    GlobalMethod(
                            returnType = type,
                            name = methodName.text,
                            args = args,
                            statement = block,
                            position = classId.position,
                            length = block.length - classId.position)
                }
    }
}