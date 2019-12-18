package pw.binom.material.compiler

import pw.binom.material.psi.SourceExp
import pw.binom.material.psi.Type

abstract class FieldDesc(val name: String, val type: TypeDesc) : Scope {
    override fun findMethod(name: String, args: List<TypeDesc>): MethodDesc? =
            type.findMethod(name, args)

    override fun findField(name: String): FieldDesc? = type.findField(name)

    override fun findType(type: Type): TypeDesc? = null

    override val parentScope: Scope?
        get() = null

    override fun findType(clazz: ClassDesc, array: List<Int>): TypeDesc? = null
}

class GlobalFieldDesc(val parent: TypeDesc?, name: String, type: TypeDesc, val source: SourceExp) : FieldDesc(name, type)
class LocalFieldDesc(name: String, type: TypeDesc) : FieldDesc(name, type)