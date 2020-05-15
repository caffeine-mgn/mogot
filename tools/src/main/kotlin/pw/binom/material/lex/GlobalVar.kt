package pw.binom.material.lex

import pw.binom.ifType
import pw.binom.material.SourcePoint
import pw.binom.skipSpace

class GlobalVar(val type: Type, val name: String, val initValue: Expression?, override val source: SourcePoint) : Global {
    companion object {
        fun read(lexer: LexStream<TokenType>): GlobalVar? =
                lexer.safe {
                    val clazz = ClassId.read(lexer) ?: return@safe null
                    val id = lexer.skipSpace()
                            ?.ifType(TokenType.ID)
                            ?: return@safe null
                    val type = ClassId.readArray(clazz, lexer) ?: return@safe null
                    var initValue: Expression? = null
                    val vv = lexer.skipSpace()
                    when (vv?.element) {
                        null, TokenType.END_LINE -> {
                            null
                        }
                        TokenType.ASSIGN -> {
                            initValue = Expression.read(lexer) ?: return@safe null
                            lexer.skipSpace()?.ifType(TokenType.END_LINE) ?: return@safe null
                        }


                        else -> return@safe null
                    }

                    GlobalVar(
                            type = type,
                            name = id.text,
                            source = SourcePoint(clazz.source.module, clazz.source.position, type.position + type.length - clazz.source.position),
                            initValue = initValue)
                }
    }
}