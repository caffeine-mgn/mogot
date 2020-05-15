package pw.binom.material.compiler

import pw.binom.material.RootModule
import pw.binom.material.SourcePoint

class StringExpressionDesc(val origenal: String, val text: String, source: SourcePoint) : ExpressionDesc(source) {
    override val resultType: TypeDesc
        get() = RootModule.stringType
    override val childs: Sequence<SourceElement>
        get() = emptySequence()
}