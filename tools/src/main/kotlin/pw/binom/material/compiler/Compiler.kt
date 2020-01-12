package pw.binom.material.compiler

import pw.binom.material.psi.*

class Compiler(val parser: Parser) : Scope {
    private val global
        get() = parser.global
    private val floatClass = ClassDesc("float")
    private val intClass = ClassDesc("int")
    private val boolClass = ClassDesc("bool")
    val vec4Class = ClassDesc("vec4")
    val gvec4Class = ClassDesc("gvec4")
    private val vec3Class = ClassDesc("vec3")
    private val vec2Class = ClassDesc("vec2")
    private val mat4Class = ClassDesc("mat4")
    private val mat3Class = ClassDesc("mat3")
    private val sampler2DClass = ClassDesc("sampler2D")
    private val classes = ArrayList<ClassDesc>()
    private val singleTypes = HashMap<ClassDesc, SingleType>()
    private val arrayTypes = HashMap<ClassDesc, MutableSet<ArrayType>>()
    val rootFields = ArrayList<GlobalFieldDesc>()
    val rootMethods = ArrayList<MethodDesc>()
    val rootClasses = ArrayList<ClassDesc>()

    init {
        classes += floatClass
        classes += vec4Class
        classes += vec3Class
        classes += vec2Class
        classes += mat3Class
        classes += mat4Class
        classes += boolClass
        classes += intClass
        classes += sampler2DClass
        classes += gvec4Class
    }

    val floatType = findType(TypePromitive(TokenType.FLOAT, emptyList())) as SingleType
    val vec3Type = findType(TypePromitive(TokenType.VEC3, emptyList())) as SingleType
    val mat4Type = findType(TypePromitive(TokenType.MAT4, emptyList())) as SingleType
    val mat3Type = findType(TypePromitive(TokenType.MAT3, emptyList())) as SingleType
    val vec4Type = findType(TypePromitive(TokenType.VEC4, emptyList())) as SingleType
    val gvec4Type = findType(TypeId("gvec4", emptyList())) as SingleType
    val vec2Type = findType(TypePromitive(TokenType.VEC2, emptyList())) as SingleType
    val intType = findType(TypePromitive(TokenType.INT, emptyList())) as SingleType
    val sampler2DType = findType(TypeId("sampler2D", emptyList())) as SingleType

    init {

        intType.methods += MethodDesc(this, null, "unarMinus", intType, listOf(), false)
        floatType.methods += MethodDesc(this, null, "unarMinus", floatType, listOf(), false)
        vec2Type.methods += MethodDesc(this, null, "unarMinus", vec2Type, listOf(), false)
        vec3Type.methods += MethodDesc(this, null, "unarMinus", vec3Type, listOf(), false)
        vec4Type.methods += MethodDesc(this, null, "unarMinus", vec4Type, listOf(), false)


        rootMethods += MethodDesc(this, null, "clamp", floatType, listOf(
                MethodDesc.Argument("a", floatType),
                MethodDesc.Argument("a", floatType),
                MethodDesc.Argument("b", floatType)
        ), false)

        rootMethods += MethodDesc(this, null, "max", floatType, listOf(
                MethodDesc.Argument("a", floatType),
                MethodDesc.Argument("b", floatType)
        ), false)

        rootMethods += MethodDesc(this, null, "pow", floatType, listOf(
                MethodDesc.Argument("a", floatType),
                MethodDesc.Argument("b", floatType)
        ), false)


        rootMethods += MethodDesc(this, null, "normalize", vec3Type, listOf(
                MethodDesc.Argument("vector", vec3Type)
        ), false)

        rootMethods += MethodDesc(this, null, "length", floatType, listOf(
                MethodDesc.Argument("vector", vec3Type)
        ), false)

        rootMethods += MethodDesc(this, null, "dot", floatType, listOf(
                MethodDesc.Argument("a", vec3Type),
                MethodDesc.Argument("b", vec3Type)
        ), false)

        rootMethods += MethodDesc(this, null, "reflect", vec3Type, listOf(
                MethodDesc.Argument("a", vec3Type),
                MethodDesc.Argument("b", vec3Type)
        ), false)

        rootMethods += MethodDesc(this, null, "vec3", vec3Type, listOf(
                MethodDesc.Argument("x", floatType),
                MethodDesc.Argument("y", floatType),
                MethodDesc.Argument("z", floatType)
        ), false)

        rootMethods += MethodDesc(this, null, "vec3", vec3Type, listOf(
                MethodDesc.Argument("matrix", mat3Type)
        ), false)

        rootMethods += MethodDesc(this, null, "vec3", vec3Type, listOf(
                MethodDesc.Argument("matrix", mat4Type)
        ), false)

        rootMethods += MethodDesc(this, null, "vec4", vec4Type, listOf(
                MethodDesc.Argument("matrix", mat4Type)
        ), false)



        rootMethods += MethodDesc(this, null, "texture", gvec4Type, listOf(
                MethodDesc.Argument("texture", sampler2DType),
                MethodDesc.Argument("uv", vec2Type)
        ), false)

        rootMethods += MethodDesc(this, null, "vec4", vec4Type, listOf(
                MethodDesc.Argument("x", floatType),
                MethodDesc.Argument("y", floatType),
                MethodDesc.Argument("z", floatType),
                MethodDesc.Argument("w", floatType)
        ), false)

        rootMethods += MethodDesc(this, null, "vec4", vec4Type, listOf(
                MethodDesc.Argument("vector", vec3Type),
                MethodDesc.Argument("w", floatType)
        ), false)

        rootMethods += MethodDesc(this, null, "inverse", mat4Type, listOf(
                MethodDesc.Argument("matrix", mat4Type)
        ), false)

        rootMethods += MethodDesc(this, null, "transpose", mat4Type, listOf(
                MethodDesc.Argument("matrix", mat4Type)
        ), false)

        rootMethods += MethodDesc(this, null, "mat3", mat3Type, listOf(
                MethodDesc.Argument("matrix", mat4Type)
        ), false)

        mat3Type.methods += MethodDesc(this, null, "times", mat3Type, listOf(
                MethodDesc.Argument("vec", vec3Type)
        ), false)

        mat4Type.methods += MethodDesc(this, null, "times", mat4Type, listOf(
                MethodDesc.Argument("vec", vec4Type)
        ), false)

        intType.methods += MethodDesc(this, null, "plus", mat4Type, listOf(
                MethodDesc.Argument("value", floatType)
        ), false)

        vec4Type.methods += MethodDesc(this, null, "times", vec4Type, listOf(
                MethodDesc.Argument("value", floatType)
        ), false)

        gvec4Type.fields += GlobalFieldDesc(gvec4Type, "rgba", vec4Type, SourceExp(0, 0))
        vec4Type.fields += GlobalFieldDesc(gvec4Type, "x", floatType, SourceExp(0, 0))
        vec4Type.fields += GlobalFieldDesc(gvec4Type, "y", floatType, SourceExp(0, 0))
        vec4Type.fields += GlobalFieldDesc(gvec4Type, "z", floatType, SourceExp(0, 0))
        vec4Type.fields += GlobalFieldDesc(gvec4Type, "w", floatType, SourceExp(0, 0))

        vec3Type.fields += GlobalFieldDesc(gvec4Type, "x", floatType, SourceExp(0, 0))
        vec3Type.fields += GlobalFieldDesc(gvec4Type, "y", floatType, SourceExp(0, 0))
        vec3Type.fields += GlobalFieldDesc(gvec4Type, "z", floatType, SourceExp(0, 0))
    }

    override fun findType(type: Type): TypeDesc {
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
        val clazz = classes.find { it.name == name }
                ?: rootClasses.find { it.name == name }
                ?: throw CompileException("Undefined class $name", type.position, type.length)
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

    override fun findType(clazz: ClassDesc, array: List<Int>): TypeDesc? {
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

    val properties = HashMap<GlobalFieldDesc, Map<String, String>>()
    var model: GlobalFieldDesc? = null
    var vertex: GlobalFieldDesc? = null
    var normal: GlobalFieldDesc? = null
    var uv: GlobalFieldDesc? = null
    var projection: GlobalFieldDesc? = null

    fun isProperty(field: GlobalFieldDesc) = properties.containsKey(field)
    fun isExternal(field: GlobalFieldDesc) =
            field == model || field == vertex || field == normal || field == uv || field == projection

    init {
        global.forEach {
            when (it) {
                is GlobalVar -> rootFields += compile(null, it)
                is GlobalMethod -> rootMethods += compile(null, it)
                is ClassDef -> rootClasses += compile(it)
                else -> TODO("Unknown element: ${it::class.java.name}")
            }
        }
    }

    private fun compile(clazz: ClassDef): ClassDesc {
        val out = ClassDesc(clazz.name)
        val type = findType(out, emptyList()) as SingleType
        clazz.methods.forEach {
            type.methods += compile(out, it)
        }

        clazz.fields.forEach {
            type.fields += compile(out, it)
        }
        return out
    }

    private fun compile(clazz: ClassDesc?, globalVar: GlobalVar): GlobalFieldDesc {
        val g = GlobalFieldDesc(
                name = globalVar.name,
                type = findType(globalVar.type),
                parent = clazz?.let { findType(it, emptyList()) },
                source = SourceExp(globalVar.position, globalVar.length)
        )
        parser.properties[globalVar]?.let {
            properties[g] = it.properties
        }
        if (parser.model == globalVar)
            model = g
        if (parser.vertex == globalVar)
            vertex = g

        if (parser.normal == globalVar)
            normal = g
        if (parser.uv == globalVar)
            uv = g
        if (parser.projection == globalVar)
            projection = g
        return g
    }

    private fun compile(clazz: ClassDesc?, method: GlobalMethod): MethodDesc {
        val out = MethodDesc(
                scope = this,
                name = method.name,
                returnType = findType(method.returnType),
                args = method.args.map {
                    MethodDesc.Argument(
                            it.name,
                            findType(it.type)
                    )
                },
                parent = clazz?.let { findType(it, emptyList()) },
                external = false
        )
        out.statementBlock = compile(out, method.statement)
        return out
    }

    private fun compile(scope: Scope, statement: StatementBlock): StatementBlockDesc {
        val out = StatementBlockDesc(scope)
        statement.statements.forEach {
            out.statements += compile(out, it)
        }
        return out
    }

    private fun compile(scope: Scope, statement: ReturnStatement): ReturnStatementDest {
        val exp = statement.exp?.let { compile(scope, null, it) }
        val method = scope.findMethod()
                ?: throw CompileException("Return statement must be use only inside method", statement.position, statement.length)
        if (exp == null) {
            if (method.returnType != scope.findType(TypePromitive(TokenType.VOID, emptyList()))!!)
                throw CompileException("Function ${method.name} must return ${method.returnType}", statement.position, statement.length)
        } else {
            if (method.returnType != exp.resultType)
                throw CompileException("Not compatible return type. Expected ${method.returnType} but factual ${exp.resultType}", statement.position, statement.length)
        }
        return ReturnStatementDest(exp)
    }

    private fun compile(scope: Scope, expression: MethodCallExpression): MethodCallExpressionDesc {
        val args = expression.args.map {
            compile(scope, null, it)
        }
        val exp = expression.exp?.let { compile(scope, null, it) }
        val method = (exp?.resultType ?: scope).findMethod(expression.method, args.map { it.resultType })
                ?: if (exp == null)
                    throw CompileException("Undefined Method ${expression.method}(${args.map { it.resultType }.joinToString(", ")})", expression.position, expression.length)
                else
                    throw CompileException("Undefined Method ${exp.resultType}.${expression.method}(${args.map { it.resultType }.joinToString(", ")})", expression.position, expression.length)
        return MethodCallExpressionDesc(method, args, exp)
    }

    private fun compile(scope: Scope, expression: NumberExpression): NumberExpressionDesc {
        val type = when {
            expression.value.endsWith("f") -> NumberExpressionDesc.Type.FLOAT
            "." in expression.value -> NumberExpressionDesc.Type.DOUBLE
            else -> NumberExpressionDesc.Type.INT
        }
        return NumberExpressionDesc(scope, expression.value.toDouble(), type)
    }

    private fun compile(scope: Scope, expression: OperationExpression): ExpressionDesc {
        val left = compile(scope, null, expression.left)
        val right = compile(scope, null, expression.right)
        if (left.resultType !== right.resultType) {
            val method = left.findMethod(expression.operator.innerMethod, listOf(right.resultType))
                    ?: throw CompileException("Undefined Operation Method for convert ${left.resultType} to ${right.resultType}", expression.position, expression.length)
            return MethodCallExpressionDesc(method, listOf(right), left)
        }
        return OperationExpressionDesc(
                left = left,
                right = right,
                operator = expression.operator
        )
    }

    private fun compile(scope: Scope, expression: IdAccessExpression): FieldAccessExpressionDesc {
        val ss = expression.exp?.let { compile(scope, null, it) } ?: scope
        val field = ss.findField(expression.id)
                ?: throw CompileException("Undefined variable ${expression.id}", expression.position, expression.length)
        return FieldAccessExpressionDesc(field, ss as? ExpressionDesc)
    }

    private fun compile(scope: Scope, expression: IncDecExpression): IncDecExpressionDesc {
        val exp = compile(scope, null, expression.exp)
        if (exp.resultType !is SingleType)
            throw CompileException("Can't apply increment to non single type", expression.position, expression.length)
        val type = exp.resultType as SingleType
        if (type != intType)
            throw CompileException("Can't apply increment to non int type", expression.position, expression.length)
        return IncDecExpressionDesc(
                exp,
                expression.operator,
                expression.prefix
        )
    }

    private fun compile(scope: Scope, expression: ArrayAccessExpression) =
            compile(scope, null, MethodCallExpression(
                    expression.exp,
                    "get",
                    listOf(expression.index),
                    expression.position, expression.length
            ))

    private fun compile(scope: Scope, expression: InvertExpression): MethodCallExpressionDesc {
        val exp = MethodCallExpression(expression.exp, "unarMinus", listOf(), expression.position, expression.length)
        return compile(scope, exp)
    }

    private fun compile(scope: Scope, expression: ExpParenthesis): ExpParenthesisDest =
            ExpParenthesisDest(compile(scope, null, expression.exp))

    private fun compile(scope: Scope, from: ExpressionDesc?, expression: Expression): ExpressionDesc =
            when (expression) {
                is MethodCallExpression -> compile(scope, expression)
                is NumberExpression -> compile(scope, expression)
                is OperationExpression -> compile(scope, expression)
                is IdAccessExpression -> compile(scope, expression)
                is BooleanExpression -> compile(scope, expression)
                is ArrayAccessExpression -> compile(scope, expression)
                is IncDecExpression -> compile(scope, expression)
                is InvertExpression -> compile(scope, expression)
                is ExpParenthesis -> compile(scope, expression)
                else -> TODO("Unknown ${expression::class.java.name}")
            }

    private fun compile(scope: Scope, expression: BooleanExpression): BooleanExpressionDesc =
            BooleanExpressionDesc(scope, expression.value)

    private fun compile(scope: Scope, statement: LocalDefineAssignStatement): AssignStatementDesc {
        val block = scope.findBlock() ?: TODO()
        val local = LocalFieldDesc(statement.name, scope.findType(statement.type)!!)
        block.fields += local
        return AssignStatementDesc(
                FieldAccessExpressionDesc(local, null),
                compile(scope, null, statement.exp),
                null
        )
    }

    private fun compile(scope: Scope, statement: AssignStatement): AssignStatementDesc {
        val left = compile(scope, null, statement.subject) as FieldAccessExpressionDesc
        val right = compile(scope, null, statement.exp)
        return AssignStatementDesc(left, right, statement.operator)
    }

    private fun compile(scope: Scope, statement: IfStatement): IfStatementDesc {
        return IfStatementDesc(
                condition = compile(scope, null, statement.condition),
                thenBlock = compile(scope, statement.thenBlock),
                elseBlock = statement?.elseBlock?.let { compile(scope, it) }
        )
    }

    private fun compile(scope: Scope, statement: ForStatement): StatementBlockDesc {
        val block = StatementBlockDesc(scope)
        statement.init?.let { compile(block, it) }?.let { block.statements += it }
        var bb = compile(block, statement.statement)

        if (statement.step != null) {
            if (bb is StatementBlockDesc) {
                bb.statements += compile(block, statement.step)
            } else {
                val v = StatementBlockDesc(block)
                v.statements += bb
                v.statements += compile(block, statement.step)
                bb = v
            }
        }
        block.statements += WhileDesc(
                statement.end?.let { compile(block, null, it) } ?: BooleanExpressionDesc(block, true),
                bb
        )
        return block
    }

    private fun compile(scope: Scope, statement: ExpStatement): StatementExprDesc {
        return StatementExprDesc(compile(scope, null, statement.exp))
    }

    private fun compile(scope: Scope, statement: Statement): StatementDesc =
            when (statement) {
                is ReturnStatement -> compile(scope, statement)
                is StatementBlock -> compile(scope, statement)
                is LocalDefineAssignStatement -> compile(scope, statement)
                is AssignStatement -> compile(scope, statement)
                is ForStatement -> compile(scope, statement)
                is IfStatement -> compile(scope, statement)
                is ExpStatement -> compile(scope, statement)
                else -> throw CompileException("Unknown type: ${statement::class.java.name}", statement.position, statement.length)
            }

    override val parentScope: Scope?
        get() = null

    override fun findMethod(name: String, args: List<TypeDesc>): MethodDesc? =
            rootMethods.find { it.canCall(name, args) }

    override fun findField(name: String): FieldDesc? =
            rootFields.find { it.name == name }?.let { return it }
}

interface Scope {
    val parentScope: Scope?
    fun findMethod(name: String, args: List<TypeDesc>): MethodDesc?
    fun findField(name: String): FieldDesc?
    fun findType(type: Type): TypeDesc?
    fun findType(clazz: ClassDesc, array: List<Int>): TypeDesc?
}

fun Scope.findBlock(): StatementBlockDesc? {
    if (this is StatementBlockDesc)
        return this
    return parentScope?.findBlock()
}

fun Scope.findMethod(): MethodDesc? {
    if (this is MethodDesc)
        return this
    return parentScope?.findMethod()
}