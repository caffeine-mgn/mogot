package pw.binom.material.lex

import pw.binom.ifType
import pw.binom.material.SourcePoint
import pw.binom.skipSpace

class LocalDefineAssignStatement(
        val type: Type,
        val name: String,
        val exp: Expression,
        val fieldSource: SourcePoint,
        override val source: SourcePoint) : UnitStatement {
    companion object {
        fun read(lexer: LexStream<TokenType>): LocalDefineAssignStatement? = lexer.safe {
            val clazz = ClassId.read(lexer) ?: return@safe null
            val id = lexer.skipSpace()?.ifType(TokenType.ID) ?: return@safe null
            val type = ClassId.readArray(clazz, lexer) ?: return@safe null
            lexer.skipSpace()?.ifType(TokenType.ASSIGN) ?: return@safe null
            val exp = Expression.read(lexer) ?: return@safe null
            LocalDefineAssignStatement(
                    type = type,
                    name = id.text,
                    exp = exp,
                    fieldSource = SourcePoint(
                            lexer.module,
                            clazz.source.position,
                            id.position + id.source().length - clazz.source.position
                    ),
                    source = SourcePoint(
                            lexer.module,
                            clazz.source.position,
                            exp.source.position + exp.source.length - clazz.source.position
                    )
            )
        }
    }
}