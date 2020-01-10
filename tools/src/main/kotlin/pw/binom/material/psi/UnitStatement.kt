package pw.binom.material.psi

import pw.binom.ifType
import pw.binom.skipSpace

interface UnitStatement:Statement{
    companion object {
        fun read(lexer: LexStream<TokenType>): UnitStatement? =
                LocalDefineAssignStatement.read(lexer)
                        ?: AssignStatement.read(lexer)
                        ?: ExpStatement.read(lexer)
    }
}