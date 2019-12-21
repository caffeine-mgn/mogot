package pw.binom.material.compiler

class MethodCallExpressionDesc(val methodDesc: MethodDesc, val args: List<ExpressionDesc>, val from: ExpressionDesc?) : ExpressionDesc() {
    override val resultType: TypeDesc
        get() = methodDesc.returnType
}