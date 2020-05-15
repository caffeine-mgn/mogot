package pw.binom.material.compiler

import pw.binom.material.SourcePoint
import pw.binom.material.lex.Type

class StatementBlockDesc(val scope: Scope?,source: SourcePoint) : StatementDesc(source), Scope {

    val fields = ArrayList<LocalFieldDesc>()
    val statements = ArrayList<StatementDesc>()
    override val parentScope: Scope?
        get() = scope

    override fun findMethod(name: String, args: List<TypeDesc>): MethodDesc? =
            scope?.findMethod(name, args)

    override fun findField(name: String): FieldDesc? =
            fields.find { it.name == name } ?: scope?.findField(name)

    override fun findType(type: Type): TypeDesc? =
            scope?.findType(type)

    override fun findType(clazz: ClassDesc, array: List<Int>): TypeDesc? =
            scope?.findType(clazz, array)

    override val childs: Sequence<SourceElement>
        get() = statements.asSequence()
}