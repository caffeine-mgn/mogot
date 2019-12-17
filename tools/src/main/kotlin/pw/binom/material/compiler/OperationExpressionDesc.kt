package pw.binom.material.compiler

import pw.binom.material.psi.OperationExpression

class OperationExpressionDesc(val left: ExpressionDesc, val right: ExpressionDesc, val operator: OperationExpression.Operator) : ExpressionDesc() {
    override val resultType: TypeDesc
        get() = left.resultType

}