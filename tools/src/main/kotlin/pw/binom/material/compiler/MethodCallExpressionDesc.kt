package pw.binom.material.compiler

import pw.binom.material.SourcePoint

class MethodCallExpressionDesc(
        val methodDesc: MethodDesc,
        val args: List<ExpressionDesc>,
        val from: ExpressionDesc?,
        source: SourcePoint) : ExpressionDesc(source) {
    override val resultType: TypeDesc
        get() = methodDesc.returnType
    override val childs: Sequence<SourceElement>
        get() = args.asSequence()
}