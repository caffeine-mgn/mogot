package pw.binom.material.compiler

import pw.binom.material.SourcePoint
import pw.binom.material.lex.Type

class FieldAccessExpressionDesc(var field: FieldDesc, val from: ExpressionDesc?, source: SourcePoint) : ExpressionDesc(source), Scope {
    override val resultType: TypeDesc
        get() = this.field.type

    override fun findMethod(name: String, args: List<TypeDesc>): MethodDesc? =
            field.findMethod(name, args)

    override fun findField(name: String): FieldDesc? =
            field.findField(name)

    override fun findType(type: Type): TypeDesc? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override val childs: Sequence<SourceElement>
        get() = sequenceOf(from).filterNotNull()


}