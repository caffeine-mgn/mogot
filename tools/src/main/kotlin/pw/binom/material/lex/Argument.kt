package pw.binom.material.lex

import pw.binom.ifType
import pw.binom.material.SourcePoint
import pw.binom.skipSpace

class Argument(val type: Type, val name: String, val source: SourcePoint) {
    companion object {
        fun read(lex: LexStream<TokenType>): Argument? =
                lex.safe {
                    val clazz = ClassId.read(lex) ?: return@safe null
                    val name = lex.skipSpace()
                            ?.ifType(TokenType.ID)
                            ?: return@safe null
                    val type = ClassId.readArray(clazz, lex) ?: return@safe null
                    Argument(type, name.text,
                            source = SourcePoint(
                                    lex.module,
                                    clazz.source.position,
                                    type.length + clazz.source.length - clazz.source.position
                            ))
                }
    }
}