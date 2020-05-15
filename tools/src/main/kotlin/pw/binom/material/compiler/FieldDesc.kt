package pw.binom.material.compiler

import pw.binom.material.SourcePoint
import pw.binom.material.lex.Type

abstract class FieldDesc(var name: String, val type: TypeDesc, override val source: SourcePoint) : Scope, SourceElement {
    override fun findMethod(name: String, args: List<TypeDesc>): MethodDesc? =
            type.findMethod(name, args)

    val annotations = ArrayList<AnnotationDesc>()

    override fun findField(name: String): FieldDesc? = type.findField(name)

    override fun findType(type: Type): TypeDesc? = null

    override val parentScope: Scope?
        get() = null

    override fun findType(clazz: ClassDesc, array: List<Int>): TypeDesc? = null
}

class GlobalFieldDesc(val parent: TypeDesc?, name: String, type: TypeDesc, val initValue: ExpressionDesc? = null, source: SourcePoint) : FieldDesc(name, type, source) {
    override val childs: Sequence<SourceElement>
        get() = initValue?.let { sequenceOf(initValue) }?: emptySequence()
}

class LocalFieldDesc(name: String, type: TypeDesc, source: SourcePoint) : FieldDesc(name, type, source){
    override val childs: Sequence<SourceElement>
        get() = emptySequence()

}