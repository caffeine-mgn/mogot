package pw.binom.material.compiler

import pw.binom.material.SourcePoint

class StatementExprDesc(val exp: ExpressionDesc, source: SourcePoint) : StatementDesc(source) {
    override val childs: Sequence<SourceElement>
        get() = sequenceOf(exp)
}