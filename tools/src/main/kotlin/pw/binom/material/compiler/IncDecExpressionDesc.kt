package pw.binom.material.compiler

import pw.binom.material.psi.OperationExpression

class IncDecExpressionDesc(
        val exp: ExpressionDesc,
        val operator: OperationExpression.Operator,
        val prefix: Boolean) : ExpressionDesc() {
    override val resultType: TypeDesc
        get() = exp.resultType
}