package pw.binom.material

import pw.binom.material.compiler.*

class DCE(val compiler: Compiler) {
    val methodsVP = LinkedHashSet<MethodDesc>()
    val methodsFP = LinkedHashSet<MethodDesc>()
    val fieldsVP = LinkedHashSet<GlobalFieldDesc>()
    val fieldsFP = LinkedHashSet<GlobalFieldDesc>()

    val classesVP = LinkedHashSet<ClassDesc>()
    val classesFP = LinkedHashSet<ClassDesc>()

    private var visitVP = true

    init {
        val vertexMethod = compiler.rootMethods.find { it.name == "vertex" }
        val fragmentMethod = compiler.rootMethods.find { it.name == "fragment" }
        if (vertexMethod != null) {
            visitVP = true
            visit(vertexMethod.statementBlock!!)
            methodsVP += vertexMethod
        }

        if (fragmentMethod != null) {
            visitVP = false
            visit(fragmentMethod.statementBlock!!)
            methodsFP += fragmentMethod
        }
    }


    private fun useClass(clazz: ClassDesc) {
        if (clazz !in compiler.rootClasses)
            return
        if (visitVP) {
            classesVP += clazz
        } else {
            classesFP += clazz
        }
    }

    private fun visit(statement: StatementBlockDesc) {
        statement.statements.forEach {
            visit(it)
        }
    }

    private fun visit(statemen: ReturnStatementDest) {
        statemen.ext?.let { visit(it) }
    }

    private fun visit(expression: MethodCallExpressionDesc) {
        expression.from?.let { visit(it) }
        expression.args.forEach {
            visit(it)
        }

        expression.methodDesc.parent?.clazz?.let {
            useClass(it)
        }

        useClass(expression.resultType.clazz)

        if (visitVP)
            methodsVP += expression.methodDesc
        else
            methodsFP += expression.methodDesc
    }

    private fun visit(expression: IncDecExpressionDesc) {
        visit(expression.exp)
    }

    private fun visit(expression: ExpParenthesisDest){
        visit(expression.exp)
    }

    private fun visit(expression: ExpressionDesc) {
        when (expression) {
            is MethodCallExpressionDesc -> visit(expression)
            is NumberExpressionDesc -> {
                //NOP
            }
            is OperationExpressionDesc -> visit(expression)
            is FieldAccessExpressionDesc -> visit(expression)
            is IncDecExpressionDesc -> visit(expression)
            is ExpParenthesisDest -> visit(expression)
            else -> TODO("->${expression::class.java.name}")
        }
    }

    private fun visit(expression: OperationExpressionDesc) {
        visit(expression.left)
        visit(expression.right)
    }

    private fun visit(expression: FieldAccessExpressionDesc) {
        useClass(expression.resultType.clazz)
        expression.from?.let { visit(it) }
        if (expression.field is GlobalFieldDesc) {
            if (visitVP)
                fieldsVP += expression.field
            else
                fieldsFP += expression.field
        } else {
            expression.from?.let { visit(it) }
        }
    }

    private fun visit(statement: AssignStatementDesc) {
        visit(statement.field)
        visit(statement.exp)
        useClass(statement.exp.resultType.clazz)
        useClass(statement.field.resultType.clazz)
        val f = statement.field as FieldAccessExpressionDesc
        (f.field as? GlobalFieldDesc)?.parent?.clazz?.let {
            useClass(it)
        }
    }

    private fun visit(statement: WhileDesc) {
        visit(statement.condition)
        visit(statement.statement)
    }

    private fun visit(statement: StatementExprDesc) {
        visit(statement.exp)
    }

    private fun visit(statement: StatementDesc) {
        when (statement) {
            is StatementBlockDesc -> visit(statement)
            is ReturnStatementDest -> visit(statement)
            is AssignStatementDesc -> visit(statement)
            is WhileDesc -> visit(statement)
            is StatementExprDesc -> visit(statement)
            else -> TODO("->${statement::class.java.name}")
        }
    }
}