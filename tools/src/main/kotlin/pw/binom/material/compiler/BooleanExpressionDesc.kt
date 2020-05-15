package pw.binom.material.compiler

import pw.binom.material.SourcePoint
import pw.binom.material.lex.TokenType
import pw.binom.material.lex.TypePromitive

class BooleanExpressionDesc(scope: Scope, val value:Boolean, source: SourcePoint) : ExpressionDesc(source) {
    override val resultType: TypeDesc = scope.findType(TypePromitive(TokenType.BOOL, emptyList()))!!
    override val childs: Sequence<SourceElement>
        get() = emptySequence()
}