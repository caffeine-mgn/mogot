package pw.binom.material.compiler

import pw.binom.material.SourcePoint
import pw.binom.material.lex.OperationExpression

class OperationExpressionDesc(val left: ExpressionDesc, val right: ExpressionDesc, val operator: OperationExpression.Operator, source: SourcePoint) : ExpressionDesc(source) {
    override val resultType: TypeDesc
        get() = left.resultType
    override val childs: Sequence<SourceElement>
        get() = sequenceOf(left, right)

}