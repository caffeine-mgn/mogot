package pw.binom.material.generator.gles300

import pw.binom.material.compiler.*
import pw.binom.material.generator.GLESGenerator
import pw.binom.material.psi.OperationExpression

class GLES300Generator(compiler: Compiler) : GLESGenerator(compiler) {

    val vp = genVP()
    val fp = genFP()

    private fun genFields(vertex: Boolean, fields: Set<GlobalFieldDesc>, sb: Appendable) {
        val classes = if (vertex)
            dce.classesVP
        else
            dce.classesFP

        classes.forEach {
            gen(it, sb)
        }


        fields.forEach {
            println()
            if (compiler.model == it
                    || compiler.projection == it
                    || compiler.properties.containsKey(it)) {
                sb.append("uniform ")
            }
            if (compiler.vertex == it) {
                sb.append("layout(location = 0) in ")
            }
            if (compiler.normal == it) {
                sb.append("layout(location = 1) in ")
            }
            if (compiler.uv == it) {
                sb.append("layout(location = 2) in ")
            }
            if (vertex) {
                if (it in dce.fieldsFP) {
                    sb.append("out ")
                }
            } else {
                if (it in dce.fieldsVP) {
                    sb.append("in ")
                }
            }
            gen(it.type, sb) {
                sb.append(" ").append(it.name)
            }
            sb.append(";\n")
        }
    }

    private fun genFP(): String {
        val sb = StringBuilder()

        sb.append("#version 300 es\n")
        sb.append("precision mediump float;\n")
        sb.append("out vec4 resultColor;\n")
        genFields(false, dce.fieldsFP, sb)

        dce.methodsFP.forEach {
            gen(it, sb)
        }
        sb.append("void main() {\n\tresultColor = fragment(vec4(0.0f,0.0f,0.0f,0.0f));\n}")
        return sb.toString()
    }

    private fun genVP(): String {
        val sb = StringBuilder()

        sb.append("#version 300 es\n")
        sb.append("precision mediump float;\n")
        genFields(true, dce.fieldsVP, sb)

        dce.methodsVP.forEach {
            gen(it, sb)
        }
        sb.append("void main() {\n\tgl_Position=vertex();\n}")
        return sb.toString()
    }

    private fun gen(clazz: ClassDesc, sb: Appendable) {
        sb.append("struct ").append(clazz.name).append("{\n")
        val type = compiler.findType(clazz, emptyList()) as SingleType
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
        sb.append(" ").append(method.glslName).append("(")
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
            OperationExpression.Operator.GE -> "<="
            OperationExpression.Operator.GT -> "<*>"
            OperationExpression.Operator.LE -> ">="
            OperationExpression.Operator.LT -> ">"
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
        sb.append(expression.field.name)
    }

    private fun gen(expression: BooleanExpressionDesc, sb: Appendable) {
        sb.append(if (expression.value) "true" else "false")
    }

    private fun gen(expression: ExpressionDesc, sb: Appendable) {
        when (expression) {
            is FieldAccessExpressionDesc -> gen(expression, sb)
            is MethodCallExpressionDesc -> gen(expression, sb)
            is NumberExpressionDesc -> gen(expression, sb)
            is OperationExpressionDesc -> gen(expression, sb)
            is BooleanExpressionDesc -> gen(expression, sb)
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
            OperationExpression.Operator.GE -> "<="
            OperationExpression.Operator.GT -> "<*>"
            OperationExpression.Operator.LE -> ">="
            OperationExpression.Operator.LT -> ">"
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

    private fun gen(expression: MethodCallExpressionDesc, sb: Appendable) {
        val method = expression.methodDesc

        if (method.name == "times" && expression.from != null && method.args.size == 1 && !method.external) {
            val arg = method.args[0].type
            val parent = expression.from.resultType
            when {
                parent == compiler.vec4Type && arg == compiler.floatType -> {
                    gen(expression.from, sb)
                    sb.append("*")
                    gen(expression.args[0], sb)
                    return
                }

                (parent == compiler.mat4Type || parent == compiler.mat3Type) && (arg == compiler.vec4Type || arg == compiler.vec3Type) -> {
                    gen(expression.from, sb)
                    sb.append("*")
                    gen(expression.args[0], sb)
                    return
                }
            }
        }
        if (method.parent is ArrayType && method.name == "get") {
            gen(expression.from!!, sb)
            expression.args.forEach {
                sb.append("[")
                gen(it, sb)
                sb.append("]")
            }
            return
        }
        if (!method.external) {
            expression.from?.let {
                gen(it, sb)
                sb.append(".")
            }
            sb.append(expression.methodDesc.glslName).append("(")
            var first = true
            expression.args.forEach {
                if (!first)
                    sb.append(", ")
                gen(it, sb)
                first = false
            }
            sb.append(")")
        } else {
            sb.append(expression.methodDesc.glslName).append("(")
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