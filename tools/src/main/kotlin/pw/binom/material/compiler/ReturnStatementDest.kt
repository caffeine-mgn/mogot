package pw.binom.material.compiler

import pw.binom.material.SourcePoint

class ReturnStatementDest(val ext: ExpressionDesc?, source: SourcePoint) : StatementDesc(source) {
    override val childs: Sequence<SourceElement>
        get() = sequenceOf(ext).filterNotNull()
}