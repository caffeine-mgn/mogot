package pw.binom.material

import pw.binom.material.compiler.*
import pw.binom.material.lex.TokenType
import pw.binom.material.lex.TypeId
import pw.binom.material.lex.TypePromitive

object RootModule : AbstractModule() {

    override val parentScope: Scope?
        get() = null

    private val classes = HashMap<String, ClassDesc>()
    val globalMethods = ArrayList<MethodDesc>()
    val zeroSource = SourcePoint(this, 0, 0)

    private fun defineClass(name: String): ClassDesc {
        val clazz = ClassDesc(name, zeroSource)
        classes[name] = clazz
        return clazz
    }

    val floatClass = defineClass("float")
    val intClass = defineClass("int")
    val boolClass = defineClass("bool")
    val vec4Class = defineClass("vec4")
    val gvec4Class = defineClass("gvec4")
    val vec3Class = defineClass("vec3")
    val vec2Class = defineClass("vec2")
    val mat4Class = defineClass("mat4")
    val mat3Class = defineClass("mat3")
    val importClass = defineClass("import")
    val propertyClass = defineClass("property")
    val vertexClass = defineClass("vertex")
    val normalClass = defineClass("normal")
    val uvClass = defineClass("uv")
    val cameraPositionClass = defineClass("cameraPosition")
    val projectionClass = defineClass("projection")
    val modelViewClass = defineClass("modelView")
    val modelClass = defineClass("model")
    val stringClass = defineClass("String")
    val sampler2DClass = defineClass("sampler2D")

    val floatType = findType(TypePromitive(TokenType.FLOAT, emptyList())) as SingleType
    val boolType = findType(TypePromitive(TokenType.BOOL, emptyList())) as SingleType
    val vec3Type = findType(TypePromitive(TokenType.VEC3, emptyList())) as SingleType
    val mat4Type = findType(TypePromitive(TokenType.MAT4, emptyList())) as SingleType
    val mat3Type = findType(TypePromitive(TokenType.MAT3, emptyList())) as SingleType
    val vec4Type = findType(TypePromitive(TokenType.VEC4, emptyList())) as SingleType
    val gvec4Type = findType(TypeId("gvec4", emptyList())) as SingleType
    val vec2Type = findType(TypePromitive(TokenType.VEC2, emptyList())) as SingleType
    val intType = findType(TypePromitive(TokenType.INT, emptyList())) as SingleType
    val sampler2DType = findType(TypeId("sampler2D", emptyList())) as SingleType
    val importType = findType(TypeId(importClass.name, emptyList())) as SingleType
    val propertyType = findType(TypeId(propertyClass.name, emptyList())) as SingleType
    val vertexType = findType(TypeId(vertexClass.name, emptyList())) as SingleType
    val normalType = findType(TypeId(normalClass.name, emptyList())) as SingleType
    val uvType = findType(TypeId(uvClass.name, emptyList())) as SingleType
    val cameraPositionType = findType(TypeId(cameraPositionClass.name, emptyList())) as SingleType
    val projectionType = findType(TypeId(projectionClass.name, emptyList())) as SingleType
    val modelViewType = findType(TypeId(modelViewClass.name, emptyList())) as SingleType
    val modelType = findType(TypeId(modelClass.name, emptyList())) as SingleType
    val stringType = findType(TypeId(stringClass.name, emptyList())) as SingleType

    private fun defineMethod(method: MethodDesc) {
        globalMethods += method
    }

    init {
        intType.methods += MethodDesc(this, null, "unarMinus", intType, listOf(), false, zeroSource)
        floatType.methods += MethodDesc(this, null, "unarMinus", floatType, listOf(), false, zeroSource)
        vec2Type.methods += MethodDesc(this, null, "unarMinus", vec2Type, listOf(), false, zeroSource)
        vec3Type.methods += MethodDesc(this, null, "unarMinus", vec3Type, listOf(), false, zeroSource)
        vec3Type.methods += MethodDesc(this, null, "times", vec3Type, listOf(MethodDesc.Argument("value", floatType, zeroSource)), false, zeroSource)
        vec3Type.methods += MethodDesc(this, null, "minus", vec3Type, listOf(MethodDesc.Argument("value", floatType, zeroSource)), false, zeroSource)
        vec4Type.methods += MethodDesc(this, null, "unarMinus", vec4Type, listOf(), false, zeroSource)


        defineMethod(MethodDesc(this, null, "dFdx", vec3Type, listOf(
                MethodDesc.Argument("p", vec3Type, zeroSource)
        ), false, zeroSource))

        defineMethod(MethodDesc(this, null, "mix", vec3Type, listOf(
                MethodDesc.Argument("p1", vec3Type, zeroSource),
                MethodDesc.Argument("p2", vec3Type, zeroSource),
                MethodDesc.Argument("p3", floatType, zeroSource)
        ), false, zeroSource))

        defineMethod(MethodDesc(this, null, "min", floatType, listOf(
                MethodDesc.Argument("p1", floatType, zeroSource),
                MethodDesc.Argument("p2", floatType, zeroSource)
        ), false, zeroSource))

        defineMethod(MethodDesc(this, null, "dFdy", vec3Type, listOf(
                MethodDesc.Argument("p", vec3Type, zeroSource)
        ), false, zeroSource))

        defineMethod(MethodDesc(this, null, "dFdx", vec2Type, listOf(
                MethodDesc.Argument("p", vec2Type, zeroSource)
        ), false, zeroSource))

        defineMethod(MethodDesc(this, null, "mat3", mat3Type, listOf(
                MethodDesc.Argument("p1", vec3Type, zeroSource),
                MethodDesc.Argument("p2", vec3Type, zeroSource),
                MethodDesc.Argument("p3", vec3Type, zeroSource)
        ), false, zeroSource))

        defineMethod(MethodDesc(this, null, "dFdy", vec2Type, listOf(
                MethodDesc.Argument("p", vec2Type, zeroSource)
        ), false, zeroSource))

        defineMethod(MethodDesc(this, null, "clamp", floatType, listOf(
                MethodDesc.Argument("a", floatType, zeroSource),
                MethodDesc.Argument("a", floatType, zeroSource),
                MethodDesc.Argument("b", floatType, zeroSource)
        ), false, zeroSource))

        defineMethod(MethodDesc(this, null, "max", floatType, listOf(
                MethodDesc.Argument("a", floatType, zeroSource),
                MethodDesc.Argument("b", floatType, zeroSource)
        ), false, zeroSource))

        defineMethod(MethodDesc(this, null, "pow", floatType, listOf(
                MethodDesc.Argument("a", floatType, zeroSource),
                MethodDesc.Argument("b", floatType, zeroSource)
        ), false, zeroSource))

        defineMethod(MethodDesc(this, null, "pow", vec3Type, listOf(
                MethodDesc.Argument("a", vec3Type, zeroSource),
                MethodDesc.Argument("b", vec3Type, zeroSource)
        ), false, zeroSource))

        defineMethod(MethodDesc(this, null, "cross", vec3Type, listOf(
                MethodDesc.Argument("a", vec3Type, zeroSource),
                MethodDesc.Argument("b", vec3Type, zeroSource)
        ), false, zeroSource))


        defineMethod(MethodDesc(this, null, "normalize", vec3Type, listOf(
                MethodDesc.Argument("vector", vec3Type, zeroSource)
        ), false, zeroSource))

        defineMethod(MethodDesc(this, null, "length", floatType, listOf(
                MethodDesc.Argument("vector", vec3Type, zeroSource)
        ), false, zeroSource))

        defineMethod(MethodDesc(this, null, "dot", floatType, listOf(
                MethodDesc.Argument("a", vec3Type, zeroSource),
                MethodDesc.Argument("b", vec3Type, zeroSource)
        ), false, zeroSource))

        defineMethod(MethodDesc(this, null, "reflect", vec3Type, listOf(
                MethodDesc.Argument("a", vec3Type, zeroSource),
                MethodDesc.Argument("b", vec3Type, zeroSource)
        ), false, zeroSource))

        defineMethod(MethodDesc(this, null, "vec3", vec3Type, listOf(
                MethodDesc.Argument("x", floatType, zeroSource),
                MethodDesc.Argument("y", floatType, zeroSource),
                MethodDesc.Argument("z", floatType, zeroSource)
        ), false, zeroSource))

        defineMethod(MethodDesc(this, null, "vec3", vec3Type, listOf(
                MethodDesc.Argument("matrix", mat3Type, zeroSource)
        ), false, zeroSource))

        defineMethod(MethodDesc(this, null, "vec3", vec3Type, listOf(
                MethodDesc.Argument("matrix", mat4Type, zeroSource)
        ), false, zeroSource))


        defineMethod(MethodDesc(this, null, "vec3", vec3Type, listOf(
                MethodDesc.Argument("value", floatType, zeroSource)
        ), false, zeroSource))

        defineMethod(MethodDesc(this, null, "vec4", vec4Type, listOf(
                MethodDesc.Argument("matrix", mat4Type, zeroSource)
        ), false, zeroSource))



        defineMethod(MethodDesc(this, null, "texture", gvec4Type, listOf(
                MethodDesc.Argument("texture", sampler2DType, zeroSource),
                MethodDesc.Argument("uv", vec2Type, zeroSource)
        ), false, zeroSource))

        defineMethod(MethodDesc(this, null, "vec4", vec4Type, listOf(
                MethodDesc.Argument("x", floatType, zeroSource),
                MethodDesc.Argument("y", floatType, zeroSource),
                MethodDesc.Argument("z", floatType, zeroSource),
                MethodDesc.Argument("w", floatType, zeroSource)
        ), false, zeroSource))

        defineMethod(MethodDesc(this, null, "vec4", vec4Type, listOf(
                MethodDesc.Argument("vector", vec3Type, zeroSource),
                MethodDesc.Argument("w", floatType, zeroSource)
        ), false, zeroSource))

        defineMethod(MethodDesc(this, null, "inverse", mat4Type, listOf(
                MethodDesc.Argument("matrix", mat4Type, zeroSource)
        ), false, zeroSource))

        defineMethod(MethodDesc(this, null, "transpose", mat4Type, listOf(
                MethodDesc.Argument("matrix", mat4Type, zeroSource)
        ), false, zeroSource))

        defineMethod(MethodDesc(this, null, "mat3", mat3Type, listOf(
                MethodDesc.Argument("matrix", mat4Type, zeroSource)
        ), false, zeroSource))

        mat3Type.methods += MethodDesc(this, null, "times", mat3Type, listOf(
                MethodDesc.Argument("vec", vec3Type, zeroSource)
        ), false, zeroSource)

        mat4Type.methods += MethodDesc(this, null, "times", mat4Type, listOf(
                MethodDesc.Argument("vec", vec4Type, zeroSource)
        ), false, zeroSource)

        intType.methods += MethodDesc(this, null, "plus", mat4Type, listOf(
                MethodDesc.Argument("value", floatType, zeroSource)
        ), false, zeroSource)

        vec4Type.methods += MethodDesc(this, null, "times", vec4Type, listOf(
                MethodDesc.Argument("value", floatType, zeroSource)
        ), false, zeroSource)

        floatType.methods += MethodDesc(this, null, "times", vec3Type, listOf(
                MethodDesc.Argument("value", vec3Type, zeroSource)
        ), false, zeroSource)

        floatType.methods += MethodDesc(this, null, "minus", vec3Type, listOf(
                MethodDesc.Argument("value", vec3Type, zeroSource)
        ), false, zeroSource)

        floatType.methods += MethodDesc(this, null, "plus", vec3Type, listOf(
                MethodDesc.Argument("value", vec3Type, zeroSource)
        ), false, zeroSource)

        intType.methods += MethodDesc(this, null, "times", floatType, listOf(
                MethodDesc.Argument("value", floatType, zeroSource)
        ), false, zeroSource)

        vec3Type.methods += MethodDesc(this, null, "div", vec3Type, listOf(
                MethodDesc.Argument("value", floatType, zeroSource)
        ), false, zeroSource)

        val zero = SourcePoint(this, 0, 0)
        gvec4Type.fields += GlobalFieldDesc(gvec4Type, "rgba", vec4Type, source = zero)
        gvec4Type.fields += GlobalFieldDesc(gvec4Type, "rgb", vec3Type, source = zero)
        gvec4Type.fields += GlobalFieldDesc(gvec4Type, "xyz", vec3Type, source = zero)
        gvec4Type.fields += GlobalFieldDesc(gvec4Type, "r", floatType, source = zero)
        gvec4Type.fields += GlobalFieldDesc(gvec4Type, "g", floatType, source = zero)
        gvec4Type.fields += GlobalFieldDesc(gvec4Type, "b", floatType, source = zero)
        gvec4Type.fields += GlobalFieldDesc(gvec4Type, "a", floatType, source = zero)

        vec4Type.fields += GlobalFieldDesc(gvec4Type, "x", floatType, source = zero)
        vec4Type.fields += GlobalFieldDesc(gvec4Type, "y", floatType, source = zero)
        vec4Type.fields += GlobalFieldDesc(gvec4Type, "z", floatType, source = zero)
        vec4Type.fields += GlobalFieldDesc(gvec4Type, "w", floatType, source = zero)

        vec4Type.fields += GlobalFieldDesc(gvec4Type, "s", floatType, source = zero)
        vec4Type.fields += GlobalFieldDesc(gvec4Type, "t", floatType, source = zero)
        vec4Type.fields += GlobalFieldDesc(gvec4Type, "p", floatType, source = zero)
        vec4Type.fields += GlobalFieldDesc(gvec4Type, "q", floatType, source = zero)

        vec3Type.fields += GlobalFieldDesc(gvec4Type, "x", floatType, source = zero)
        vec3Type.fields += GlobalFieldDesc(gvec4Type, "y", floatType, source = zero)
        vec3Type.fields += GlobalFieldDesc(gvec4Type, "z", floatType, source = zero)
        vec3Type.fields += GlobalFieldDesc(vec3Type, "xyz", vec3Type, source = zero)
        vec3Type.fields += GlobalFieldDesc(vec3Type, "xy", vec3Type, source = zero)
        vec3Type.fields += GlobalFieldDesc(vec3Type, "xz", vec3Type, source = zero)

        vec2Type.fields += GlobalFieldDesc(vec2Type, "s", floatType, source = zero)
        vec2Type.fields += GlobalFieldDesc(vec2Type, "t", floatType, source = zero)
        vec2Type.fields += GlobalFieldDesc(vec2Type, "p", floatType, source = zero)

        importType.fields += GlobalFieldDesc(importType, "file", stringType, source = zero)
        propertyType.fields += GlobalFieldDesc(propertyType, "hidden", boolType, source = zero)
        propertyType.fields += GlobalFieldDesc(propertyType, "value", stringType, source = zero)
        propertyType.fields += GlobalFieldDesc(propertyType, "title", stringType, source = zero)
        propertyType.fields += GlobalFieldDesc(propertyType, "max", floatType, source = zero)
        propertyType.fields += GlobalFieldDesc(propertyType, "min", floatType, source = zero)
        propertyType.fields += GlobalFieldDesc(propertyType, "step", floatType, source = zero)
    }

    override fun findClass(name: String): ClassDesc? = classes[name]
    override fun isClassOwen(clazz: ClassDesc): Boolean =
            classes[clazz.name] == clazz

    override fun findMethod(name: String, args: List<TypeDesc>): MethodDesc? =
            globalMethods.find { it.canCall(name, args) }

    override fun findField(name: String): FieldDesc? = null
}