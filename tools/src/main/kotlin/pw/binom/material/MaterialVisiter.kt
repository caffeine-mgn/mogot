package pw.binom.material

import pw.binom.material.compiler.*

interface MaterialVisiter {
    fun visit(statement: StatementBlockDesc) {
        statement.statements.forEach {
            visit(it)
        }
    }

    fun visit(statement: ReturnStatementDest) {
        statement.ext?.let { visit(it) }
    }

    fun visit(expression: MethodCallExpressionDesc) {
        expression.from?.let { visit(it) }
        expression.args.forEach {
            visit(it)
        }
    }

    fun visit(expression: IncDecExpressionDesc) {
        visit(expression.exp)
    }

    fun visit(expression: ExpParenthesisDest) {
        visit(expression.exp)
    }

    fun visit(expression: NumberExpressionDesc) {

    }

    fun visit(expression: ExpressionDesc) {
        when (expression) {
            is MethodCallExpressionDesc -> visit(expression)
            is NumberExpressionDesc -> visit(expression)
            is OperationExpressionDesc -> visit(expression)
            is FieldAccessExpressionDesc -> visit(expression)
            is IncDecExpressionDesc -> visit(expression)
            is ExpParenthesisDest -> visit(expression)
            else -> TODO("->${expression::class.java.name}")
        }
    }

    fun visit(expression: OperationExpressionDesc) {
        visit(expression.left)
        visit(expression.right)
    }

    fun visit(expression: FieldAccessExpressionDesc) {
        expression.from?.let { visit(it) }
    }

    fun visit(statement: AssignStatementDesc) {
        visit(statement.field)
        visit(statement.exp)
    }

    fun visit(statement: WhileDesc) {
        visit(statement.condition)
        visit(statement.statement)
    }

    fun visit(statement: StatementExprDesc) {
        visit(statement.exp)
    }

    fun visit(statement: IfStatementDesc) {
        visit(statement.condition)
        visit(statement.thenBlock)
        statement.elseBlock?.let { visit(it) }
    }

    fun visit(module: SourceModule) {
        module.globalMethods.forEach {
            it.statementBlock?.let { visit(it) }
        }
    }

    fun visit(statement: StatementDesc) {
        when (statement) {
            is StatementBlockDesc -> visit(statement)
            is ReturnStatementDest -> visit(statement)
            is AssignStatementDesc -> visit(statement)
            is WhileDesc -> visit(statement)
            is StatementExprDesc -> visit(statement)
            is IfStatementDesc -> visit(statement)
            else -> TODO("->${statement::class.java.name}")
        }
    }
}