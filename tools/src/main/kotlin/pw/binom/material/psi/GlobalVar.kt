package pw.binom.material.psi

import pw.binom.ifType
import pw.binom.material.compiler.ClassDesc
import pw.binom.skipSpace

class GlobalVar(val type: Type, val name: String, override val position: Int, override val length: Int) : Global {
    companion object {
        fun read(lexer: LexStream<TokenType>): GlobalVar? =
                lexer.safe {
                    val clazz = ClassId.read(lexer) ?: return@safe null
                    val id = lexer.skipSpace()
                            ?.ifType(TokenType.ID)
                            ?: return@safe null
                    val type = ClassId.readArray(clazz, lexer) ?: return@safe null
                    lexer.skipSpace()
                            ?.ifType(TokenType.END_LINE)
                            ?: return@safe null
                    GlobalVar(type = type, name = id.text, position = clazz.position, length = type.position + type.length - clazz.position)
                }
    }
}