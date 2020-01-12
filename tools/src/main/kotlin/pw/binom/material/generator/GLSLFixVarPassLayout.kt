package pw.binom.material.generator

import pw.binom.material.DCE
import pw.binom.material.MaterialVisiter
import pw.binom.material.compiler.*
import pw.binom.material.psi.Global
import pw.binom.material.psi.GlobalMethod
import pw.binom.material.psi.OperationExpression

object GLSLFixVarPassLayout {
    fun fix(compiler: Compiler) {
        val vertexMethod = compiler.rootMethods.find { it.name == "vertex" }

        val c = DCE(compiler)
        val vertex = c.fieldsFP.find {
            compiler.vertex == it
        }

        val uv = c.fieldsFP.find {
            compiler.uv == it
        }
        val normal = c.fieldsFP.find {
            compiler.normal == it
        }
        if (vertex != null) {
            val replace = GlobalFieldDesc(null, "${vertex.name}_inout", vertex.type, vertex.source)
            FieldReplaceVisiter(vertex, replace).visit(compiler)
            FieldAccessExpressionDesc(replace, FieldAccessExpressionDesc(vertex, null))
            vertexMethod!!.statementBlock!!.statements.add(0, AssignStatementDesc(FieldAccessExpressionDesc(replace, null), FieldAccessExpressionDesc(vertex, null), null))
        }

        if (normal != null) {
            val replace = GlobalFieldDesc(null, "${normal.name}_inout", normal.type, normal.source)
            FieldReplaceVisiter(normal, replace).visit(compiler)
            FieldAccessExpressionDesc(replace, FieldAccessExpressionDesc(normal, null))
            vertexMethod!!.statementBlock!!.statements.add(0, AssignStatementDesc(FieldAccessExpressionDesc(replace, null), FieldAccessExpressionDesc(normal, null), null))
        }

        if (uv != null) {
            val replace = GlobalFieldDesc(null, "${uv.name}_inout", uv.type, uv.source)
            FieldReplaceVisiter(uv, replace).visit(compiler)
            FieldAccessExpressionDesc(replace, FieldAccessExpressionDesc(uv, null))
            vertexMethod!!.statementBlock!!.statements.add(0, AssignStatementDesc(FieldAccessExpressionDesc(replace, null), FieldAccessExpressionDesc(uv, null), null))
        }
    }
}

interface GlobalVisiter {
    fun visit(global: MethodDesc)
    fun visit(clazz: ClassDesc)
    fun visit(field: GlobalFieldDesc)
}

interface MethodVisiter {
    fun visit(method: GlobalMethod)
}

class FieldReplaceVisiter(val target: GlobalFieldDesc, val replace: GlobalFieldDesc) : MaterialVisiter {
    override fun visit(expression: FieldAccessExpressionDesc) {
        if (expression.field===target) {
            expression.field = replace
        }
        super.visit(expression)
    }
}