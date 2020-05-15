package pw.binom.material.compiler

import pw.binom.material.SourcePoint

class ExpParenthesisDest(val exp: ExpressionDesc, source: SourcePoint) : ExpressionDesc(source) {
    override val resultType: TypeDesc
        get() = exp.resultType

    override val childs: Sequence<SourceElement>
        get() = sequenceOf(exp)
}