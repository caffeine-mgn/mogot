package pw.binom.material.psi

import pw.binom.checkNext
import pw.binom.ifType
import pw.binom.skipSpace

class ClassId(val element: LexStream.Element<TokenType>, val position: Int, val length: Int) {

    companion object {
        fun read(lexer: LexStream<TokenType>) = lexer.safe {
            val element = lexer.skipSpace(true)
                    ?.takeIf { it.element.isPrimitive || it.element == TokenType.ID }
                    ?: return@safe null
            ClassId(element, element.position, element.text.length)
        }

        fun readArray(clazz: ClassId, lexer: LexStream<TokenType>) = lexer.safe {
            val index = ArrayList<Int>()
            var start: LexStream.Element<TokenType>? = null
            var end: LexStream.Element<TokenType>? = null
            while (true) {
                val ll = lexer.checkNext()
                if (ll?.element != TokenType.LEFT_INDEX) {
                    break
                }
                if (start == null && ll.element == TokenType.LEFT_INDEX) {
                    start = ll
                }
                lexer.skipSpace()
                index += NumberExpression.read(lexer)?.value?.toInt() ?: return@safe null
                end = lexer.skipSpace()?.ifType(TokenType.RIGHT_INDEX) ?: return@safe null
            }

            val startPos = start?.position ?: clazz.position
            val endPos = end?.position?.let { it + 1 } ?: clazz.position + clazz.length

            if (clazz.element.element.isPrimitive)
                TypePromitive(clazz.element.element, index, startPos, endPos - startPos)
            else
                TypeId(clazz.element.text, index, startPos, endPos - startPos)
        }
    }
}