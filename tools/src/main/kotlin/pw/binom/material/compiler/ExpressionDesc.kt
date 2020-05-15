package pw.binom.material.compiler

import pw.binom.material.SourcePoint
import pw.binom.material.lex.Type

abstract class ExpressionDesc(override val source: SourcePoint) : Scope, SourceElement {
    abstract val resultType: TypeDesc

    override fun findMethod(name: String, args: List<TypeDesc>): MethodDesc? =
            resultType.findMethod(name, args)

    override fun findField(name: String): FieldDesc? =
            resultType.findField(name)

    override fun findType(type: Type): TypeDesc? =
            null

    override val parentScope: Scope?
        get() = null

    override fun findType(clazz: ClassDesc, array: List<Int>): TypeDesc? =
            null
}