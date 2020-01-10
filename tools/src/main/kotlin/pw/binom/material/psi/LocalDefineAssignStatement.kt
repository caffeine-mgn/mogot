package pw.binom.material.psi

import pw.binom.ifType
import pw.binom.skipSpace

class LocalDefineAssignStatement(
        val type: Type,
        val name: String,
        val exp: Expression,
        override val position: Int,
        override val length: Int) : UnitStatement {
    companion object {
        fun read(lexer: LexStream<TokenType>): LocalDefineAssignStatement? = lexer.safe {
            val clazz = ClassId.read(lexer) ?: return@safe null
            val id = lexer.skipSpace()?.ifType(TokenType.ID) ?: return@safe null
            val type = ClassId.readArray(clazz, lexer) ?: return@safe null
            lexer.skipSpace()?.ifType(TokenType.ASSIGN) ?: return@safe null
            val exp = Expression.read(lexer) ?: return@safe null
            LocalDefineAssignStatement(type, id.text, exp, clazz.position, exp.position + exp.length - clazz.position)
        }
    }
}