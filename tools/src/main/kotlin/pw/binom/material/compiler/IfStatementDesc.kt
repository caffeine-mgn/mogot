package pw.binom.material.compiler

import pw.binom.material.SourcePoint

class IfStatementDesc(
        val condition: ExpressionDesc,
        val thenBlock: StatementDesc,
        val elseBlock: StatementDesc?,
        source: SourcePoint
) : StatementDesc(source) {
    override val childs: Sequence<SourceElement>
        get() = sequenceOf(condition, thenBlock, elseBlock).filterNotNull()
}