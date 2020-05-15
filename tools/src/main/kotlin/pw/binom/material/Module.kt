package pw.binom.material

import pw.binom.material.compiler.*
import pw.binom.material.lex.Type
import pw.binom.material.lex.TypeId
import pw.binom.material.lex.TypePromitive

interface Module : Scope

abstract class AbstractModule : Module {
    protected abstract fun findClass(name: String): ClassDesc?
    protected abstract fun isClassOwen(clazz: ClassDesc):Boolean

    private val singleTypes = HashMap<ClassDesc, SingleType>()
    private val arrayTypes = HashMap<ClassDesc, MutableSet<ArrayType>>()

    override fun findType(clazz: ClassDesc, array: List<Int>): TypeDesc? {
        if (!isClassOwen(clazz))
            return null
        if (array.isEmpty()) {
            return singleTypes.getOrPut(clazz) { SingleType(clazz) }
        } else {
            val set = arrayTypes.getOrPut(clazz) { HashSet() }
            var v = set.find {
                it.size.size == array.size
                it.size.forEachIndexed { index1, i -> if (array[index1] != i) return@find false }
                true
            }
            if (v == null) {
                v = ArrayType(this, clazz, array)
                set += v
            }
            return v
        }
    }

    override fun findType(type: Type): TypeDesc? {
        val name = when (type) {
            is TypePromitive -> type.type.name.toLowerCase()
            is TypeId -> type.type
            else -> TODO()
        }
        val index = when (type) {
            is TypePromitive -> type.index
            is TypeId -> type.index
            else -> TODO()
        }
        val clazz = findClass(name)?: return null//throw CompileException("Undefined class $name", type.position, type.length)
        return if (index.isEmpty()) {
            singleTypes.getOrPut(clazz) { SingleType(clazz) }
        } else {
            val set = arrayTypes.getOrPut(clazz) { HashSet() }
            var v = set.find {
                it.size.size == index.size
                it.size.forEachIndexed { index1, i -> if (index[index1] != i) return@find false }
                true
            }
            if (v == null) {
                v = ArrayType(this, clazz, index)
                set += v
            }
            v
        }
    }
}