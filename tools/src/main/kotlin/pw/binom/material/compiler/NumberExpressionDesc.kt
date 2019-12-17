package pw.binom.material.compiler

import pw.binom.material.psi.TokenType
import pw.binom.material.psi.TypePromitive

class NumberExpressionDesc(val scope: Scope, val value: Double, val type: Type) : ExpressionDesc() {
    enum class Type {
        INT, FLOAT, DOUBLE
    }

    override val resultType: TypeDesc = scope.findType(TypePromitive(
            when (type) {
                Type.DOUBLE -> TokenType.DOUBLE
                Type.FLOAT -> TokenType.FLOAT
                Type.INT -> TokenType.INT
            },
            emptyList()
    ))!!
}