package pw.binom.material.compiler

import pw.binom.material.SourcePoint
import pw.binom.material.lex.TokenType
import pw.binom.material.lex.TypePromitive

class NumberExpressionDesc(val scope: Scope, val value: Double, val type: Type, source: SourcePoint) : ExpressionDesc(source) {
    enum class Type {
        INT, FLOAT, DOUBLE
    }

    override val resultType: TypeDesc = scope.findType(TypePromitive(
            when (type) {
                Type.DOUBLE -> TokenType.DOUBLE
                Type.FLOAT -> TokenType.FLOAT
                Type.INT -> TokenType.INT
            },
            emptyList()
    ))!!
    override val childs: Sequence<SourceElement>
        get() = emptySequence()
}