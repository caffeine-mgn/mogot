package pw.binom.material.compiler

import pw.binom.material.psi.OperationExpression

class AssignStatementDesc(val field: FieldAccessExpressionDesc, val exp: ExpressionDesc, val operator: OperationExpression.Operator?) : StatementDesc()