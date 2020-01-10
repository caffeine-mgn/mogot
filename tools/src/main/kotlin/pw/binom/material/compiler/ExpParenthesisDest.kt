package pw.binom.material.compiler

class ExpParenthesisDest(val exp: ExpressionDesc) : ExpressionDesc() {
    override val resultType: TypeDesc
        get() = exp.resultType
}