package pw.binom.material.compiler

import pw.binom.material.lex.Type

class AnnotationClassDesc: Scope {

    override val parentScope: Scope?
        get() = null

    override fun findMethod(name: String, args: List<TypeDesc>): MethodDesc? = null


    override fun findField(name: String): FieldDesc? = null


    override fun findType(type: Type): TypeDesc? = null
    override fun findType(clazz: ClassDesc, array: List<Int>): TypeDesc? = null
}