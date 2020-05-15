package pw.binom.material.lex

interface Type {
    val position: Int
    val length: Int
    val index: List<Int>

    companion object {
        /*
        fun read(lexer: LexStream<TokenType>): Type? =
                lexer.safe {
                    val type = lexer.skipSpace(true)
                            ?.takeIf { it.element.isPrimitive || it.element == TokenType.ID }
                            ?: return@safe null

                    val index = ArrayList<NumberExpression>()
                    while (true) {
                        if (lexer.checkNext()?.element != TokenType.LEFT_INDEX) {
                            break
                        }
                        lexer.skipSpace()
                        index += NumberExpression.read(lexer) ?: return@safe null
                        lexer.skipSpace()?.ifType(TokenType.RIGHT_INDEX) ?: return@safe null
                    }
                    if (type.element == TokenType.ID) {
                        return@safe TypeId(type.text, index)
                    }

                    if (type.element.isPrimitive) {
                        return@safe TypePromitive(type.element, index)
                    }
                    null
                }
         */
    }
}

class VarType(override val position: Int, override val length: Int) : Type {
    override val index: List<Int>
        get() = emptyList()
}

class ValType(override val position: Int, override val length: Int) : Type {
    override val index: List<Int>
        get() = emptyList()
}

class TypePromitive(val type: TokenType, override val index: List<Int>, override val position: Int, override val length: Int) : Type {
    constructor(type: TokenType, index: List<Int>) : this(type, index, 0, 0)
}

class TypeId(val type: String, override val index: List<Int>, override val position: Int, override val length: Int) : Type {
    constructor(type: String, index: List<Int>) : this(type, index, 0, 0)
}