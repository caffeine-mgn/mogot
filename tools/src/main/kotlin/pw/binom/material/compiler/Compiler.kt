package pw.binom.material.compiler

import pw.binom.material.ModuleResolver
import pw.binom.material.RootModule
import pw.binom.material.SourceModule
import pw.binom.material.lex.*

class Compiler(val parser: Parser, val module: SourceModule, val resolver: ModuleResolver) {
    private val global
        get() = parser.global

    private val classes = ArrayList<ClassDesc>()
    private val singleTypes = HashMap<ClassDesc, SingleType>()
    private val arrayTypes = HashMap<ClassDesc, MutableSet<ArrayType>>()

//    val properties = HashMap<GlobalFieldDesc, Map<String, String>>()

    init {

//        parser.imports.forEach {
//            module.addModule(resolver.getModule(currentFilePath, it)
//                    ?: throw RuntimeException("Can't find module \"$it\""))
//        }
        val e = global.iterator()
        while (e.hasNext()) {
            val it = e.next()
            when (it) {
                is GlobalVar -> module.defineField(compile(null, it))
                is GlobalMethod -> module.defineMethod(compile(null, it))
                is AnnotationExp -> {
                    val ann = compile(it)
                    when (ann.clazz) {
                        RootModule.importType -> {
                            val fileName = (it.body["file"] as StringExpression).string
                            module.addModule(
                                    resolver.getModule(module.currentFilePath, fileName)
                                            ?: throw CompileException("Can't find module \"$it\"", it.source)
                            )
                        }
                        RootModule.vertexType -> {
                            val f = e.next() as GlobalVar
                            val field = compile(null, f)
                            module.vertex = field
                            module.defineField(field)
                        }
                        RootModule.normalType -> {
                            val f = e.next() as GlobalVar
                            val field = compile(null, f)
                            module.normal = field
                            module.defineField(field)
                        }
                        RootModule.uvType -> {
                            val f = e.next() as GlobalVar
                            val field = compile(null, f)
                            module.uv = field
                            module.defineField(field)
                        }
                        RootModule.cameraPositionType -> {
                            val f = e.next() as GlobalVar
                            val field = compile(null, f)
                            module.camera = field
                            module.defineField(field)
                        }
                        RootModule.projectionType -> {
                            val f = e.next() as GlobalVar
                            val field = compile(null, f)
                            module.projection = field
                            module.defineField(field)
                        }
                        RootModule.modelViewType -> {
                            val f = e.next() as GlobalVar
                            val field = compile(null, f)
                            module.modelView = field
                            module.defineField(field)
                        }
                        RootModule.modelType -> {
                            val f = e.next() as GlobalVar
                            val field = compile(null, f)
                            module.model = field
                            module.defineField(field)
                        }
                        RootModule.propertyType -> {
                            val f = e.next() as GlobalVar
                            val field = compile(null, f)
                            field.annotations += ann
                            module.defineField(field)
                        }
                    }
                }
                is ClassDef -> compile(it)
                else -> TODO("Unknown element: ${it::class.java.name}")
            }
        }
    }

    private fun compile(exp: AnnotationExp): AnnotationDesc {
        val type = findType(exp.type) as SingleType
        return AnnotationDesc(
                clazz = type,
                properties = exp.body.asSequence().map {
                    val field = type.findField(it.key)
                            ?: throw CompileException("Can't find field \"${it.key}\" in class ${type.clazz.name}", it.value.source)
                    val value = compile(module, null, it.value)
                    field to value
                }.toMap(),
                source = exp.source
        )
    }

    private fun compile(clazz: ClassDef): ClassDesc {
        val out = ClassDesc(clazz.name, clazz.source)
        module.defineClass(out)
        val type = findType(out, emptyList()) as SingleType
        clazz.methods.forEach {
            type.methods += compile(out, it)
        }

        clazz.fields.forEach {
            type.fields += compile(out, it)
        }
        return out
    }

    private fun findType(clazz: ClassDesc, array: List<Int>) = module.findType(clazz, array)
            ?: throw RuntimeException("Can't find class ${clazz.name}")

    private fun compile(clazz: ClassDesc?, globalVar: GlobalVar): GlobalFieldDesc {
        val g = GlobalFieldDesc(
                name = globalVar.name,
                type = findType(globalVar.type),
                parent = clazz?.let { module.findType(it, emptyList()) },
                source = globalVar.source,
                initValue = globalVar.initValue?.let { compile(module, null, it) }
        )
        /*
        parser.properties[globalVar]?.let {
            properties[g] = it
        }
        if (parser.model == globalVar)
            module.model = g
        if (parser.modelView == globalVar)
            module.modelView = g
        if (parser.vertex == globalVar)
            module.vertex = g

        if (parser.normal == globalVar)
            module.normal = g
        if (parser.uv == globalVar)
            module.uv = g
        if (parser.projection == globalVar)
            module.projection = g
        if (parser.camera == globalVar)
            module.camera = g
         */
        return g
    }


    private fun findType(type: Type): TypeDesc {
        val result = module.findType(type)
        if (result == null) {
            val name = when (type) {
                is TypePromitive -> type.type.name.toLowerCase()
                is TypeId -> type.type
                else -> TODO()
            }
            throw CompileException("Undefined class $name", type.position, type.length)
        }
        return result
    }

    private fun compile(clazz: ClassDesc?, method: GlobalMethod): MethodDesc {
        if (method.name == "makeIt")
            println()
        val out = MethodDesc(
                scope = module,
                name = method.name,
                returnType = findType(method.returnType),
                args = method.args.map {
                    MethodDesc.Argument(
                            it.name,
                            findType(it.type),
                            it.source
                    )
                },
                parent = clazz?.let { findType(it, emptyList()) },
                external = false,
                source = method.source
        )
        out.statementBlock = compile(out, method.statement)
        return out
    }

    private fun compile(scope: Scope, statement: StatementBlock): StatementBlockDesc {
        val out = StatementBlockDesc(scope, statement.source)
        statement.statements.forEach {
            out.statements += compile(out, it)
        }
        return out
    }

    private fun compile(scope: Scope, statement: ReturnStatement): ReturnStatementDest {
        val exp = statement.exp?.let { compile(scope, null, it) }
        val method = scope.findMethod()
                ?: throw CompileException("Return statement must be use only inside method", statement.source)
        if (exp == null) {
            if (method.returnType != scope.findType(TypePromitive(TokenType.VOID, emptyList()))!!)
                throw CompileException("Function ${method.name} must return ${method.returnType}", statement.source)
        } else {
            if (method.returnType != exp.resultType)
                throw CompileException("Not compatible return type. Expected ${method.returnType} but factual ${exp.resultType}", statement.source)
        }
        return ReturnStatementDest(exp, statement.source)
    }

    private fun compile(scope: Scope, expression: MethodCallExpression): MethodCallExpressionDesc {
        val args = expression.args.map {
            compile(scope, null, it)
        }
        val exp = expression.exp?.let { compile(scope, null, it) }
        val method = (exp?.resultType ?: scope).findMethod(expression.method, args.map { it.resultType })
                ?: if (exp == null)
                    throw CompileException("Undefined Method ${expression.method}(${args.map { it.resultType }.joinToString(", ")})", expression.source)
                else
                    throw CompileException("Undefined Method ${exp.resultType}.${expression.method}(${args.map { it.resultType }.joinToString(", ")})", expression.source)
        return MethodCallExpressionDesc(method, args, exp, expression.source)
    }

    private fun compile(scope: Scope, expression: StringExpression) =
            StringExpressionDesc(
                    origenal = expression.text,
                    text = expression.string,
                    source = expression.source
            )

    private fun compile(scope: Scope, expression: NumberExpression): NumberExpressionDesc {
        val type = when {
            expression.value.endsWith("f") -> NumberExpressionDesc.Type.FLOAT
            "." in expression.value -> NumberExpressionDesc.Type.DOUBLE
            else -> NumberExpressionDesc.Type.INT
        }
        return NumberExpressionDesc(scope, expression.value.toDouble(), type, expression.source)
    }

    private fun compile(scope: Scope, expression: OperationExpression): ExpressionDesc {
        val left = compile(scope, null, expression.left)
        val right = compile(scope, null, expression.right)
        if (left.resultType !== right.resultType) {
            val method = left.findMethod(expression.operator.innerMethod, listOf(right.resultType))
                    ?: throw CompileException("Can't find method ${left.resultType}::${expression.operator.innerMethod}(${right.resultType})", expression.source)
            return MethodCallExpressionDesc(method, listOf(right), left, expression.source)
        }
        return OperationExpressionDesc(
                left = left,
                right = right,
                operator = expression.operator,
                source = expression.source
        )
    }

    private fun compile(scope: Scope, expression: IdAccessExpression): FieldAccessExpressionDesc {
        val ss = expression.exp?.let { compile(scope, null, it) } ?: scope
        val field = ss.findField(expression.id)
                ?: throw CompileException("Undefined variable ${expression.id}", expression.source)
        return FieldAccessExpressionDesc(field, ss as? ExpressionDesc, expression.source)
    }

    private fun compile(scope: Scope, expression: IncDecExpression): IncDecExpressionDesc {
        val exp = compile(scope, null, expression.exp)
        if (exp.resultType !is SingleType)
            throw CompileException("Can't apply increment to non single type", expression.source)
        val type = exp.resultType as SingleType
        if (type != RootModule.intType)
            throw CompileException("Can't apply increment to non int type", expression.source)
        return IncDecExpressionDesc(
                exp,
                expression.operator,
                expression.prefix,
                expression.source
        )
    }

    private fun compile(scope: Scope, expression: ArrayAccessExpression) =
            compile(scope, null, MethodCallExpression(
                    expression.exp,
                    "get",
                    listOf(expression.index),
                    expression.source
            ))

    private fun compile(scope: Scope, expression: InvertExpression): MethodCallExpressionDesc {
        val exp = MethodCallExpression(expression.exp, "unarMinus", listOf(), expression.source)
        return compile(scope, exp)
    }

    private fun compile(scope: Scope, expression: ExpParenthesis): ExpParenthesisDest =
            ExpParenthesisDest(compile(scope, null, expression.exp), expression.source)

    private fun compile(scope: Scope, from: ExpressionDesc?, expression: Expression): ExpressionDesc =
            when (expression) {
                is MethodCallExpression -> compile(scope, expression)
                is NumberExpression -> compile(scope, expression)
                is StringExpression -> compile(scope, expression)
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
            BooleanExpressionDesc(scope, expression.value, expression.source)

    private fun compile(scope: Scope, statement: LocalDefineAssignStatement): AssignStatementDesc {
        val block = scope.findBlock() ?: TODO()
        val local = LocalFieldDesc(statement.name, scope.findType(statement.type)!!, statement.fieldSource)
        block.fields += local
        return AssignStatementDesc(
                field = FieldAccessExpressionDesc(local, null, statement.fieldSource),
                exp = compile(scope, null, statement.exp),
                operator = null,
                source = statement.source

        )
    }

    private fun compile(scope: Scope, statement: AssignStatement): AssignStatementDesc {
        val left = compile(scope, null, statement.subject) as FieldAccessExpressionDesc
        val right = compile(scope, null, statement.exp)
        return AssignStatementDesc(left, right, statement.operator, statement.source)
    }

    private fun compile(scope: Scope, statement: IfStatement): IfStatementDesc {
        return IfStatementDesc(
                condition = compile(scope, null, statement.condition),
                thenBlock = compile(scope, statement.thenBlock),
                elseBlock = statement.elseBlock?.let { compile(scope, it) },
                source = statement.source
        )
    }

    private fun compile(scope: Scope, statement: ForStatement): StatementBlockDesc {
        val block = StatementBlockDesc(scope, statement.source)
        statement.init?.let { compile(block, it) }?.let { block.statements += it }
        var bb = compile(block, statement.statement)

        if (statement.step != null) {
            if (bb is StatementBlockDesc) {
                bb.statements += compile(block, statement.step)
            } else {
                val v = StatementBlockDesc(block, bb.source)
                v.statements += bb
                v.statements += compile(block, statement.step)
                bb = v
            }
        }
        block.statements += WhileDesc(
                condition = statement.end?.let { compile(block, null, it) }
                        ?: BooleanExpressionDesc(block, true, RootModule.zeroSource),
                statement = bb,
                source = statement.source
        )
        return block
    }

    private fun compile(scope: Scope, statement: ExpStatement): StatementExprDesc {
        return StatementExprDesc(compile(scope, null, statement.exp), statement.source)
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
                else -> throw CompileException("Unknown type: ${statement::class.java.name}", statement.source)
            }
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