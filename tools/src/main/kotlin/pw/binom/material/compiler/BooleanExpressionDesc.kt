package pw.binom.material.compiler

import pw.binom.material.psi.TokenType
import pw.binom.material.psi.TypePromitive

class BooleanExpressionDesc(scope: Scope, val value:Boolean) : ExpressionDesc() {
    override val resultType: TypeDesc = scope.findType(TypePromitive(TokenType.BOOL, emptyList()))!!
}