package pw.binom.material.compiler

import pw.binom.material.SourcePoint
import pw.binom.material.lex.OperationExpression

class AssignStatementDesc(val field: FieldAccessExpressionDesc, val exp: ExpressionDesc, val operator: OperationExpression.Operator?, source: SourcePoint) : StatementDesc(source) {
    override val childs: Sequence<SourceElement>
        get() = sequenceOf(this.field, exp).filterNotNull()
}