package pw.binom.material.compiler

import pw.binom.material.SourcePoint

class NullExpressionDesc(source: SourcePoint) : ExpressionDesc(source) {
    override val resultType: TypeDesc
        get() = TODO()
    override val childs: Sequence<SourceElement>
        get() = emptySequence()

}