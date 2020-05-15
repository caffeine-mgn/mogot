package pw.binom.material.compiler

import pw.binom.material.SourcePoint
import pw.binom.material.lex.OperationExpression

class IncDecExpressionDesc(
        val exp: ExpressionDesc,
        val operator: OperationExpression.Operator,
        val prefix: Boolean,
        source:SourcePoint) : ExpressionDesc(source) {
    override val resultType: TypeDesc
        get() = exp.resultType

    override val childs: Sequence<SourceElement>
        get() = sequenceOf(exp)
}