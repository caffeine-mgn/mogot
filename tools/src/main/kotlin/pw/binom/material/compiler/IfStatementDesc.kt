package pw.binom.material.compiler

class IfStatementDesc(
        val condition: ExpressionDesc,
        val thenBlock: StatementDesc,
        val elseBlock: StatementDesc?
) : StatementDesc()