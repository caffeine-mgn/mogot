package pw.binom.material.psi

import pw.binom.ifType
import pw.binom.skipSpace

class Argument(val type: Type, val name: String) {
    companion object {
        fun read(lex: LexStream<TokenType>): Argument? =
                lex.safe {
                    val clazz = ClassId.read(lex) ?: return@safe null
                    val name = lex.skipSpace()
                            ?.ifType(TokenType.ID)
                            ?: return@safe null
                    val type = ClassId.readArray(clazz, lex) ?: return@safe null
                    Argument(type, name.text)
                }
    }
}