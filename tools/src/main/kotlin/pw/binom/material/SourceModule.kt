package pw.binom.material

import pw.binom.material.compiler.*
import pw.binom.material.lex.Type

inline class Property(val ann: AnnotationDesc) {
    val value: String?
        get() {
            val valueField = ann.clazz.findField("value")!!
            val valueExp = (ann.properties[valueField] as? StringExpressionDesc)
            return valueExp?.text
        }

    val title: String?
        get() {
            val valueField = ann.clazz.findField("title")!!
            val valueExp = (ann.properties[valueField] as? StringExpressionDesc)
            return valueExp?.text
        }

    val max: Double?
        get() {
            val valueField = ann.clazz.findField("max")!!
            val valueExp = (ann.properties[valueField] as? NumberExpressionDesc)
            return valueExp?.value
        }

    val min: Double?
        get() {
            val valueField = ann.clazz.findField("min")!!
            val valueExp = (ann.properties[valueField] as? NumberExpressionDesc)
            return valueExp?.value
        }
    val step: Double?
        get() {
            val valueField = ann.clazz.findField("step")!!
            val valueExp = (ann.properties[valueField] as? NumberExpressionDesc)
            return valueExp?.value
        }
    val source
        get() = ann.source
}

val GlobalFieldDesc.property
    get() = annotations.asSequence().filter { it.clazz == RootModule.propertyType }.map { Property(it) }.firstOrNull()

class SourceModule(val currentFilePath: String): AbstractModule() {
    val globalMethods = ArrayList<MethodDesc>()
    val globalClasses = HashMap<String, ClassDesc>()
    val globalFields = HashMap<String, GlobalFieldDesc>()
    val moduls = ArrayList<Module>()

    val properties
        get() = globalFields.values.asSequence()
                .filter { it.annotations.asSequence().any { it.clazz == RootModule.propertyType } }

    var model: GlobalFieldDesc? = null
    var modelView: GlobalFieldDesc? = null
    var vertex: GlobalFieldDesc? = null
    var camera: GlobalFieldDesc? = null
    var normal: GlobalFieldDesc? = null
    var uv: GlobalFieldDesc? = null
    var projection: GlobalFieldDesc? = null

    fun defineField(field: GlobalFieldDesc) {
        if (globalFields.containsKey(field.name))
            throw RuntimeException("Redefine field ${field.name}")
        globalFields[field.name] = field
    }

    fun defineClass(clazz: ClassDesc) {
        if (globalClasses.containsKey(clazz.name))
            throw RuntimeException("Redefine class ${clazz.name}")
        globalClasses[clazz.name] = clazz
    }

    fun defineMethod(method: MethodDesc) {
        globalMethods += method
    }

    fun addModule(module: Module) {
        moduls += module
    }

    override fun findClass(name: String): ClassDesc? = globalClasses[name]

    override fun isClassOwen(clazz: ClassDesc): Boolean =
            globalClasses[clazz.name] == clazz

    override val parentScope: Scope?
        get() = null

    override fun findType(clazz: ClassDesc, array: List<Int>): TypeDesc? =
            super.findType(clazz, array)
                    ?: moduls.asSequence().map { it.findType(clazz, array) }.firstOrNull()
                    ?: RootModule.findType(clazz, array)

    override fun findType(type: Type): TypeDesc? =
            super.findType(type)
                    ?: moduls.asSequence().map { it.findType(type) }.firstOrNull()
                    ?: RootModule.findType(type)

    override fun findMethod(name: String, args: List<TypeDesc>): MethodDesc? =
            globalMethods.find { it.canCall(name, args) }
                    ?: moduls.asSequence().map { it.findMethod(name, args) }.firstOrNull()
                    ?: RootModule.findMethod(name, args)

    override fun findField(name: String): FieldDesc? =
            globalFields[name]
                    ?: moduls.asSequence().map { it.findField(name) }.firstOrNull()
                    ?: RootModule.findField(name)
}