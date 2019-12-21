package pw.binom.material.compiler

import pw.binom.material.psi.Type

class MethodDesc(val scope: Scope,
                 val parent: TypeDesc?,
                 val name: String,
                 val returnType: TypeDesc,
                 val args: List<Argument>,
                 val external: Boolean) : Scope {
    class Argument(name: String, type: TypeDesc) : FieldDesc(name, type)

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