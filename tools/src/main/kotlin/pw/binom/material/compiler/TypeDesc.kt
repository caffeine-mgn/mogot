package pw.binom.material.compiler

import pw.binom.material.RootModule
import pw.binom.material.lex.TokenType
import pw.binom.material.lex.Type
import pw.binom.material.lex.TypePromitive

abstract class TypeDesc(val clazz: ClassDesc) : Scope
class SingleType(clazz: ClassDesc) : TypeDesc(clazz) {

    val methods = ArrayList<MethodDesc>()
    val fields = ArrayList<FieldDesc>()

    override val parentScope: Scope?
        get() = null

    override fun findMethod(name: String, args: List<TypeDesc>): MethodDesc? =
            methods.find { it.canCall(name, args) }

    override fun findField(name: String): FieldDesc? =
            fields.find { it.name == name }

    override fun findType(type: Type): TypeDesc? = null
    override fun findType(clazz: ClassDesc, array: List<Int>): TypeDesc? = null

    override fun toString(): String = clazz.name
}

class ArrayType(scope: Scope, clazz: ClassDesc, val size: List<Int>) : TypeDesc(clazz) {
    override val parentScope: Scope?
        get() = null

    val sizeMethod = GlobalFieldDesc(
            parent = this,
            name = "size",
            type = scope.findType(TypePromitive(TokenType.INT, emptyList()))!!,
            source = RootModule.zeroSource
    )
    val getMethod = run {
        val type = if (size.size == 1)
            scope.findType(clazz, emptyList())!!
        else {
            scope.findType(clazz, size.subList(0, size.lastIndex - 1))!!
        }
        MethodDesc(
                scope, this, "get", type,
                listOf(
                        MethodDesc.Argument(
                                "index",
                                scope.findType(TypePromitive(TokenType.INT, emptyList()))!!,
                                RootModule.zeroSource
                        )
                ),
                false,
                RootModule.zeroSource)
    }

    override fun findMethod(name: String, args: List<TypeDesc>): MethodDesc? =
            if (getMethod.canCall(name, args))
                getMethod
            else
                null

    override fun findField(name: String): FieldDesc? {
        if (name == "size") return sizeMethod
        return null
    }

    override fun findType(type: Type): TypeDesc? =
            null

    override fun findType(clazz: ClassDesc, array: List<Int>): TypeDesc? = null

    override fun toString(): String = "${clazz.name}${size.map { "[$it]" }.joinToString("")}"
}