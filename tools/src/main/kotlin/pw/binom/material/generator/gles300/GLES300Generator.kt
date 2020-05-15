package pw.binom.material.generator.gles300

import pw.binom.material.RootModule
import pw.binom.material.SourceModule
import pw.binom.material.compiler.*
import pw.binom.material.generator.GLESGenerator
import pw.binom.material.lex.OperationExpression
import java.math.BigInteger
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.HashSet

private val MethodDesc.alias
    get() =
        if (statementBlock == null)
            name
        else
            if (this.parent == null)
                "m${name}_${hashCode()}"
            else
                "${parent.clazz.name}_${name}_${hashCode()}"


class GLES300Generator private constructor(module: SourceModule) : GLESGenerator(module) {

    class GeneratedResult(val vp: String, val fp: String)

    companion object {
        const val VAR_PROJECTION = "gles_projection"
        const val VAR_VERTEX = "gles_vertex"
        const val VAR_NORMAL = "gles_normal"
        const val VAR_UV = "gles_uv"
        const val VAR_MODEL = "gles_model"
        const val VAR_MODEL_VIEW = "gles_model_view"
        const val VAR_CAMERA_POSITION = "gles_camera_position"

        fun mix(compiler: List<SourceModule>): GeneratedResult {
            val gens = compiler.map { GLES300Generator(it) }

            val sb = StringBuilder()
            sb.append("#version 300 es\n")
            sb.append("precision mediump float;\n")
            sb.append("out vec4 resultColor;\n")
            if (gens.any { it.isUseModel }) {
                sb.append("uniform mat4 $VAR_MODEL;\n")
            }

            if (gens.any { it.isUseModelView }) {
                sb.append("uniform mat4 $VAR_MODEL_VIEW;\n")
            }

            if (gens.any { it.isUseCamera }) {
                sb.append("uniform vec3 $VAR_CAMERA_POSITION;\n")
            }
            gens.forEach {
                sb.append(it.fp)
            }
            sb.append("void main() {\n")
            sb.append("resultColor=vec4(0.0f,0.0f,0.0f,0.0f);\n")
            gens.forEach {
                if (it.fragmentMethod != null)
                    sb.append("resultColor = ${it.fragmentMethod.alias}(resultColor);\n")
            }
            sb.append("}")
            val fp = sb.toString()
            sb.clear()
            sb.append("#version 300 es\n")
            sb.append("precision mediump float;\n")

            if (gens.any { it.isUseVertex }) {
                sb.append("layout(location = 0) in vec3 $VAR_VERTEX;\n")
            }

            if (gens.any { it.isUseNormal }) {
                sb.append("layout(location = 1) in vec3 $VAR_NORMAL;\n")
            }

            if (gens.any { it.isUseUV }) {
                sb.append("layout(location = 2) in vec2 $VAR_UV;\n")
            }

            if (gens.any { it.isUseModel }) {
                sb.append("uniform mat4 $VAR_MODEL;\n")
            }
            if (gens.any { it.isUseModelView }) {
                sb.append("uniform mat4 $VAR_MODEL_VIEW;\n")
            }
            if (gens.any { it.isUseCamera }) {
                sb.append("uniform vec3 $VAR_CAMERA_POSITION;\n")
            }

            if (gens.any { it.isUseProjection }) {
                sb.append("uniform mat4 $VAR_PROJECTION;\n")
            }

            gens.forEach {
                sb.append(it.vp)
            }
            sb.append("void main() {\n\tgl_Position=")
            sb.append(gens.mapNotNull { it.vertexMethod?.alias?.let { "$it()" } }.joinToString("+"))
            sb.append(";\n")
            sb.append("}")

            return GeneratedResult(
                    vp = sb.toString(),
                    fp = fp
            )
        }
    }

    private val id = run {
        val maxLimit = BigInteger("5000000000000")
        val minLimit = BigInteger("25000000000")
        val bigInteger: BigInteger = maxLimit.subtract(minLimit)
        val randNum = Random()
        val len: Int = maxLimit.bitLength()
        var res = BigInteger(len, randNum)

        if (res < minLimit)
            res = res.add(minLimit)
        if (res >= bigInteger)
            res = res.mod(bigInteger).add(minLimit)
        res.toString(16)
    }

    val fragmentMethod: MethodDesc?
    val vertexMethod: MethodDesc?

    private val methodNameAliase = HashMap<MethodDesc, String>()


    //change methodsNames
    init {
        fragmentMethod = dce.methodsFP.find { it.name == "fragment" }
        vertexMethod = dce.methodsVP.find { it.name == "vertex" }
        val changed = HashSet<Any>()
        dce.methodsFP.forEach {
            if (it.statementBlock != null && it !in changed) {
                changed += it
            }
        }

        dce.methodsVP.forEach {
            if (it.statementBlock != null && it !in changed) {
                changed += it
            }
        }
/*
        dce.fieldsFP.forEach {
            if (!compiler.isExternal(it) && !compiler.isProperty(it) && it !in changed) {
                it.name = "f$id${it.name}"
                changed += it
            }
        }

        dce.fieldsVP.forEach {
            if (!compiler.isExternal(it) && !compiler.isProperty(it) && it.parent == null && it !in changed) {
                it.name = "f$id${it.name}"
                changed += it
            }
        }
        */
        /*
        (dce.fieldsVP.asSequence() + dce.fieldsFP.asSequence()).forEach {
            when (it) {
                compiler.module.vertex -> it.name = VAR_VERTEX
                compiler.module.normal -> it.name = VAR_NORMAL
                compiler.module.projection -> it.name = VAR_PROJECTION
                compiler.module.model -> it.name = VAR_MODEL
                compiler.module.modelView -> it.name = VAR_MODEL_VIEW
                compiler.module.uv -> it.name = VAR_UV
                compiler.module.camera -> it.name = VAR_CAMERA_POSITION
                else -> {
                    if (!compiler.isExternal(it) && !compiler.isProperty(it) && it.parent == null && it !in changed) {
                        it.name = "f$id${it.name}"
                        changed += it
                    }
                }
            }
        }
        */
    }

    fun fieldName(field: FieldDesc) = when (field) {
        module.vertex -> VAR_VERTEX
        module.normal -> VAR_NORMAL
        module.projection -> VAR_PROJECTION
        module.model -> VAR_MODEL
        module.modelView -> VAR_MODEL_VIEW
        module.uv -> VAR_UV
        module.camera -> VAR_CAMERA_POSITION
        else -> {
            if (!isExternal(field) && !isProperty(field) && (field as? GlobalFieldDesc)?.parent == null) {
                "f$id${field.name}"
            }
            field.name
        }
    }

    val vp = genVP()
    val fp = genFP()
    /*
        protected val MethodDesc.genName: String
            get() {
                if (this.statementBlock == null)
                    return name
                return "m$id$glslName"
            }
    */

    val isUseVertex
        get() = (dce.fieldsFP.asSequence() + dce.fieldsVP.asSequence()).any { module.vertex == it }

    val isUseNormal
        get() = (dce.fieldsFP.asSequence() + dce.fieldsVP.asSequence()).any { module.normal == it }

    val isUseProjection
        get() = (dce.fieldsFP.asSequence() + dce.fieldsVP.asSequence()).any { module.projection == it }

    val isUseModel
        get() = (dce.fieldsFP.asSequence() + dce.fieldsVP.asSequence()).any { module.model == it }

    val isUseModelView
        get() = (dce.fieldsFP.asSequence() + dce.fieldsVP.asSequence()).any { module.modelView == it }

    val isUseUV
        get() = (dce.fieldsFP.asSequence() + dce.fieldsVP.asSequence()).any { module.uv == it }

    val isUseCamera
        get() = (dce.fieldsFP.asSequence() + dce.fieldsVP.asSequence()).any { module.camera == it }

    private fun genFields(vertex: Boolean, fields: Set<GlobalFieldDesc>, sb: Appendable) {
        val classes = if (vertex)
            dce.classesVP
        else
            dce.classesFP

        classes.forEach {
            gen(it, sb)
        }


        fields.asSequence().filter { it.parent == null }.filter { !isExternal(it) }.forEach {

            if (module.model == it
                    || module.projection == it
                    || module.camera == it
                    || module.modelView == it
                    || isProperty(it)) {
                sb.append("uniform ")
            }
            when {
                module.vertex == it -> sb.append("layout(location = 0) in ")
                module.normal == it -> sb.append("layout(location = 1) in ")
                module.uv == it -> sb.append("layout(location = 2) in ")
                else -> {
                    if (vertex) {
                        if (it in dce.fieldsFP) {
                            sb.append("out ")
                        }
                    } else {
                        if (it in dce.fieldsVP) {
                            sb.append("in ")
                        }
                    }
                }
            }

            val variableName = when {
                module.vertex == it -> VAR_VERTEX
                module.normal == it -> VAR_NORMAL
                module.uv == it -> VAR_UV
                module.projection == it -> VAR_PROJECTION
                module.model == it -> VAR_MODEL
                module.modelView == it -> VAR_MODEL_VIEW
                module.camera == it -> VAR_CAMERA_POSITION
                else -> it.name
            }

            gen(it.type, sb) {
                sb.append(" ").append(variableName)
            }
            if (it.initValue != null) {
                sb.append("=")
                gen(it.initValue, sb)
            }
            sb.append(";\n")
        }
    }

    private fun genFP(): String {
        val sb = StringBuilder()

//        sb.append("#version 300 es\n")
//        sb.append("precision mediump float;\n")

        genFields(false, dce.fieldsFP, sb)

        dce.methodsFP.forEach {
            gen(it, sb)
        }

        return sb.toString()
    }

    private fun genVP(): String {
        val sb = StringBuilder()

//        sb.append("#version 300 es\n")
//        sb.append("precision mediump float;\n")
        genFields(true, dce.fieldsVP, sb)

        dce.methodsVP.forEach {
            gen(it, sb)
        }
//        sb.append("void main() {\n\tgl_Position=vertex();\n}")
        return sb.toString()
    }

    private fun gen(clazz: ClassDesc, sb: Appendable) {
        sb.append("struct ").append(clazz.name).append("{\n")
        val type = module.findType(clazz, emptyList()) as SingleType
        type.fields.forEach {
            sb.append("    ")
            gen(it.type, sb) {
                sb.append(" ").append(it.name)
            }
            sb.append(";\n")
        }
        sb.append("};\n")
    }

    private fun gen(method: MethodDesc, sb: Appendable) {
        if (method.statementBlock == null) {
            //-------TODO----что-то делать с методами без тела----//
            return
        }
        gen(method.returnType, sb) {
            //NOP
        }
        sb.append(" ").append(method.alias).append("(")
        var first = true
        if (method.parent != null) {
            first = false
            sb.append("${method.parent.clazz.name} _self")
        }
        method.args.forEach {
            if (!first)
                sb.append(", ")
            gen(it.type, sb) {
                sb.append(" ").append(it.name)
            }
            first = false
        }
        sb.append(")\n")
        gen(method.statementBlock!!, sb)
    }

    private fun gen(statement: WhileDesc, sb: Appendable) {
        sb.append("while (")
        gen(statement.condition, sb)
        sb.append(")\n")
        gen(statement.statement, sb)
    }

    private fun gen(statement: IfStatementDesc, sb: Appendable) {
        sb.append("if (")
        gen(statement.condition, sb)
        sb.append(")")
        gen(statement.thenBlock, sb)
        if (statement.elseBlock != null) {
            sb.append(" else ")
            gen(statement.elseBlock, sb)
        }
    }

    private fun gen(statement: StatementExprDesc, sb: Appendable) {
        gen(statement.exp, sb)
        sb.append(";\n")
    }

    private fun gen(statement: StatementDesc, sb: Appendable) {
        when (statement) {
            is StatementBlockDesc -> gen(statement, sb)
            is AssignStatementDesc -> gen(statement, sb)
            is ReturnStatementDest -> gen(statement, sb)
            is WhileDesc -> gen(statement, sb)
            is StatementExprDesc -> gen(statement, sb)
            is IfStatementDesc -> gen(statement, sb)
            else -> TODO("->${statement::class.java.name}")
        }
    }

    private fun gen(statement: AssignStatementDesc, sb: Appendable) {
        gen(statement.field, sb)
        when (statement.operator) {
            null -> null
            OperationExpression.Operator.TIMES -> "*"
            OperationExpression.Operator.PLUS -> "+"
            OperationExpression.Operator.MINUS -> "-"
            OperationExpression.Operator.DIV -> "/"
            OperationExpression.Operator.GE -> ">="
            OperationExpression.Operator.GT -> ">"
            OperationExpression.Operator.LE -> "<="
            OperationExpression.Operator.LT -> "<"
            OperationExpression.Operator.NE -> "!="
            OperationExpression.Operator.EQ -> "=="
            OperationExpression.Operator.AND -> "&&"
            OperationExpression.Operator.OR -> "||"
        }?.let { sb.append(it) }
        sb.append("=")
        gen(statement.exp, sb)
        sb.append(";\n")
    }

    private fun gen(expression: FieldAccessExpressionDesc, sb: Appendable) {
        expression.from?.let {
            gen(it, sb)
            sb.append(".")
        }
        sb.append(fieldName(expression.field))
    }

    private fun gen(expression: BooleanExpressionDesc, sb: Appendable) {
        sb.append(if (expression.value) "true" else "false")
    }

    private fun gen(expression: IncDecExpressionDesc, sb: Appendable) {
        val op = when (expression.operator) {
            OperationExpression.Operator.PLUS -> "++"
            OperationExpression.Operator.MINUS -> "--"
            else -> throw IllegalArgumentException("Unknown unar operator ${expression.operator}")
        }
        if (expression.prefix)
            sb.append(op)
        gen(expression.exp, sb)
        if (!expression.prefix)
            sb.append(op)
    }

    private fun gen(expression: ExpParenthesisDest, sb: Appendable) {
        sb.append("(")
        gen(expression.exp, sb)
        sb.append(")")
    }

    private fun gen(expression: ExpressionDesc, sb: Appendable) {
        when (expression) {
            is FieldAccessExpressionDesc -> gen(expression, sb)
            is MethodCallExpressionDesc -> gen(expression, sb)
            is NumberExpressionDesc -> gen(expression, sb)
            is OperationExpressionDesc -> gen(expression, sb)
            is BooleanExpressionDesc -> gen(expression, sb)
            is IncDecExpressionDesc -> gen(expression, sb)
            is ExpParenthesisDest -> gen(expression, sb)
            else -> TODO("Unknown Expression: ${expression::class.java.name}")
        }
    }

    private fun gen(expression: OperationExpressionDesc, sb: Appendable) {
        gen(expression.left, sb)
        val op = when (expression.operator) {
            OperationExpression.Operator.TIMES -> "*"
            OperationExpression.Operator.PLUS -> "+"
            OperationExpression.Operator.MINUS -> "-"
            OperationExpression.Operator.DIV -> "/"
            OperationExpression.Operator.GE -> ">="
            OperationExpression.Operator.GT -> ">"
            OperationExpression.Operator.LE -> "<="
            OperationExpression.Operator.LT -> "<"
            OperationExpression.Operator.NE -> "!="
            OperationExpression.Operator.EQ -> "=="
            OperationExpression.Operator.AND -> "&&"
            OperationExpression.Operator.OR -> "||"
        }
        sb.append(op)
        gen(expression.right, sb)

    }

    private fun gen(statement: ReturnStatementDest, sb: Appendable) {
        sb.append("return")
        statement.ext?.let {
            sb.append(" ")
            gen(it, sb)
        }
        sb.append(";\n")
    }

    private fun gen(expression: NumberExpressionDesc, sb: Appendable) {
        val text = when (expression.type) {
            NumberExpressionDesc.Type.DOUBLE -> expression.value.toString()
            NumberExpressionDesc.Type.FLOAT -> "${expression.value}f"
            NumberExpressionDesc.Type.INT -> "${expression.value.toInt()}"
        }
        sb.append(text)
    }

    private fun genGenericMethodCall(expression: MethodCallExpressionDesc, sb: Appendable): Boolean {
        val method = expression.methodDesc
        if (method.parent is ArrayType && method.name == "get") {
            gen(expression.from!!, sb)
            expression.args.forEach {
                sb.append("[")
                gen(it, sb)
                sb.append("]")
            }
            return true
        }

        if (method.name in OperationExpression.Operator.methods && expression.from != null && method.args.size == 1 && !method.external) {
            val arg = method.args[0].type
            val parent = expression.from.resultType
            when {
                (parent == RootModule.vec4Type && arg == RootModule.floatType)
                        || parent == RootModule.floatType && arg == RootModule.vec4Type -> {
                    gen(expression.from, sb)
                    sb.append("*")
                    gen(expression.args[0], sb)
                    return true
                }

                (parent == RootModule.vec3Type && arg == RootModule.floatType)
                        || (parent == RootModule.floatType && arg == RootModule.vec3Type) -> {
                    gen(expression.from, sb)
                    sb.append("*")
                    gen(expression.args[0], sb)
                    return true
                }

                (parent == RootModule.mat4Type || parent == RootModule.mat3Type) && (arg == RootModule.vec4Type || arg == RootModule.vec3Type) -> {
                    gen(expression.from, sb)
                    sb.append("*")
                    gen(expression.args[0], sb)
                    return true
                }
            }
        }

        if (method.name == "unarMinus" && expression.args.isEmpty() && expression.from?.resultType != null) {

            val fromType = expression.from.resultType
            if (fromType == RootModule.intType
                    || fromType == RootModule.floatType
                    || fromType == RootModule.vec2Type
                    || fromType == RootModule.vec3Type
                    || fromType == RootModule.vec4Type) {
                sb.append("-")
                gen(expression.from, sb)
                return true
            }
        }
        return false
    }

    private fun gen(expression: MethodCallExpressionDesc, sb: Appendable) {
        if (genGenericMethodCall(expression, sb))
            return
        val method = expression.methodDesc



        if (!method.external) {
            expression.from?.let {
                gen(it, sb)
                sb.append(".")
            }
            sb.append(expression.methodDesc.alias).append("(")
            var first = true
            expression.args.forEach {
                if (!first)
                    sb.append(", ")
                gen(it, sb)
                first = false
            }
            sb.append(")")
        } else {
            sb.append(expression.methodDesc.alias).append("(")
            expression.from?.let {
                gen(it, sb)
            } ?: sb.append("null")
            expression.args.forEach {
                sb.append(", ")
                gen(it, sb)
            }
            sb.append(")")
        }
    }

    private fun gen(statement: StatementBlockDesc, sb: Appendable) {
        sb.append("{\n")
        statement.fields.forEach {
            gen(it.type, sb) {
                sb.append(" ").append(it.name)
            }
            sb.append(";\n")
        }
        statement.statements.forEach {
            gen(it, sb)
        }
        sb.append("}\n")
    }

    private fun gen(type: TypeDesc, sb: Appendable, nameGen: () -> Unit) {
        sb.append(type.clazz.name)
        nameGen()
        if (type is ArrayType) {
            type.size.forEach {
                sb.append("[").append(it.toString()).append("]")
            }
        }
    }

}