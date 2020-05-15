package pw.binom.glsl

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.lang.parameterInfo.*
import org.jetbrains.kotlin.idea.util.module
import pw.binom.ModuleHolder
import pw.binom.glsl.psi.GLSLCallMethodExp
import pw.binom.glsl.psi.GLSLStatement
import pw.binom.glsl.psi.GLSLTypes
import pw.binom.material.compiler.MethodDesc


class GLSLParameterInfoHandler : ParameterInfoHandler<GLSLCallMethodExp, Any> {
    override fun showParameterInfo(element: GLSLCallMethodExp, context: CreateParameterInfoContext) {
        context.showHint(element, element.textRange.startOffset, this)
    }

    override fun updateParameterInfo(parameterOwner: GLSLCallMethodExp, context: UpdateParameterInfoContext) {
        if (context.parameterOwner !== parameterOwner) {
            context.removeHint()
            return
        }

        val parameterList = parameterOwner.callParams ?: return
        val index = ParameterInfoUtils.getCurrentParameterIndex(parameterList.getNode(), context.offset, GLSLTypes.COMMA)
        context.setCurrentParameter(index)
    }

    override fun updateUI(p: Any?, context: ParameterInfoUIContext) {
        p as MethodDesc

        val buffer = StringBuilder()

        buffer.append(p.returnType.clazz.name)
                .append(' ').append(p.name).append('(')
        val parameters = p.args
        val currentParameter = context.currentParameterIndex
        var highlightStartOffset = -1
        var highlightEndOffset = -1

        for (i in parameters.indices) {
            if (i == currentParameter) highlightStartOffset = buffer.length
            buffer.append(parameters[i].type.clazz.name).append(' ').append(parameters[i].name)
            if (i == currentParameter) highlightEndOffset = buffer.length
            if (i < parameters.size - 1) buffer.append(", ")
        }
        buffer.append(')')

        context.setupUIComponentPresentation(buffer.toString(), highlightStartOffset, highlightEndOffset, false, false, false, context.defaultParameterColor)
    }

    override fun getParametersForLookup(item: LookupElement?, context: ParameterInfoContext?): Array<Any>? =
            emptyArray()

    override fun couldShowInLookup(): Boolean = true

    override fun findElementForUpdatingParameterInfo(context: UpdateParameterInfoContext): GLSLCallMethodExp? {
        val call = ParameterInfoUtils.findParentOfTypeWithStopElements(
                context.file,
                context.offset,
                GLSLCallMethodExp::class.java,
                GLSLStatement::class.java
        )
        context.parameterOwner = call
        return call
    }

    override fun findElementForParameterInfo(context: CreateParameterInfoContext): GLSLCallMethodExp? {
        val call = ParameterInfoUtils.findParentOfTypeWithStopElements(context.file, context.offset,
                GLSLCallMethodExp::class.java, GLSLStatement::class.java)
                ?: return null
        val bb = ModuleHolder.getInstance(context.file.module!!)
                .resolver.getModule(context.file.virtualFile)
        val allMethods = bb.findAllMethodsByName(call.accessId.text)
        context.itemsToShow = allMethods.toTypedArray()
        return call
    }

    override fun getParameterCloseChars(): String? =
            ParameterInfoUtils.DEFAULT_PARAMETER_CLOSE_CHARS
}