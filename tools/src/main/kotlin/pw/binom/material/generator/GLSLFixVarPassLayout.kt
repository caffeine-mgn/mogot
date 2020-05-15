package pw.binom.material.generator

import pw.binom.material.DCE
import pw.binom.material.MaterialVisiter
import pw.binom.material.RootModule
import pw.binom.material.SourceModule
import pw.binom.material.compiler.*
import pw.binom.material.lex.GlobalMethod

object GLSLFixVarPassLayout {
    fun fix(module: SourceModule) {
        val vertexMethod = module.globalMethods.find { it.name == "vertex" }

        val c = DCE(module)
        val vertex = c.fieldsFP.find {
            module.vertex == it
        }

        val uv = c.fieldsFP.find {
            module.uv == it
        }
        val normal = c.fieldsFP.find {
            module.normal == it
        }
        if (vertex != null) {
            val replace = GlobalFieldDesc(null, "${vertex.name}_inout", vertex.type, source=vertex.source)
            FieldReplaceVisiter(vertex, replace).visit(module)
            FieldAccessExpressionDesc(replace, FieldAccessExpressionDesc(vertex, null, RootModule.zeroSource), RootModule.zeroSource)
            vertexMethod!!.statementBlock!!.statements.add(0, AssignStatementDesc(FieldAccessExpressionDesc(replace, null, RootModule.zeroSource), FieldAccessExpressionDesc(vertex, null, RootModule.zeroSource), null, RootModule.zeroSource))
        }

        if (normal != null) {
            val replace = GlobalFieldDesc(null, "${normal.name}_inout", normal.type, source=normal.source)
            FieldReplaceVisiter(normal, replace).visit(module)
            FieldAccessExpressionDesc(replace, FieldAccessExpressionDesc(normal, null, RootModule.zeroSource), RootModule.zeroSource)
            vertexMethod!!.statementBlock!!.statements.add(0, AssignStatementDesc(FieldAccessExpressionDesc(replace, null, RootModule.zeroSource), FieldAccessExpressionDesc(normal, null, RootModule.zeroSource), null, RootModule.zeroSource))
        }

        if (uv != null) {
            val replace = GlobalFieldDesc(null, "${uv.name}_inout", uv.type, source=uv.source)
            FieldReplaceVisiter(uv, replace).visit(module)
            FieldAccessExpressionDesc(replace, FieldAccessExpressionDesc(uv, null, RootModule.zeroSource), RootModule.zeroSource)
            vertexMethod!!.statementBlock!!.statements.add(0, AssignStatementDesc(FieldAccessExpressionDesc(replace, null, RootModule.zeroSource), FieldAccessExpressionDesc(uv, null, RootModule.zeroSource), null, RootModule.zeroSource))
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