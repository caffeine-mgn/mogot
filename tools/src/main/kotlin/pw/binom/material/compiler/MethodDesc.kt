package pw.binom.material.compiler

import pw.binom.material.SourcePoint
import pw.binom.material.lex.Type

class MethodDesc(val scope: Scope,
                 val parent: TypeDesc?,
                 var name: String,
                 val returnType: TypeDesc,
                 val args: List<Argument>,
                 val external: Boolean,
                 override val source: SourcePoint) : Scope, SourceElement {
    class Argument(name: String, type: TypeDesc, source:SourcePoint) : FieldDesc(name, type, source) {
        override val childs: Sequence<SourceElement>
            get() = emptySequence()
    }


    override val childs: Sequence<SourceElement>
        get() = args.asSequence()

    var statementBlock: StatementBlockDesc? = null

    override val parentScope: Scope?
        get() = null

    override fun findMethod(name: String, args: List<TypeDesc>): MethodDesc? =
            scope.findMethod(name, args)

    override fun findField(name: String): FieldDesc? =
            args.find { it.name == name }?.let { return it } ?: scope.findField(name)

    override fun findType(type: Type): TypeDesc? =
            scope.findType(type)

    override fun findType(clazz: ClassDesc, array: List<Int>): TypeDesc? =
            scope.findType(clazz, array)

    fun canCall(name: String, args: List<TypeDesc>): Boolean {
        if (name != this.name || this.args.size != args.size)
            return false
        args.forEachIndexed { index, typeDesc ->
            if (typeDesc !== this.args[index].type)
                return false
        }
        return true
    }
}