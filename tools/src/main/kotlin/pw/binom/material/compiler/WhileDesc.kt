package pw.binom.material.compiler

import pw.binom.material.SourcePoint

class WhileDesc(val condition: ExpressionDesc, val statement: StatementDesc, source: SourcePoint) : StatementDesc(source) {
    override val childs: Sequence<SourceElement>
        get() = sequenceOf(condition, statement)
}