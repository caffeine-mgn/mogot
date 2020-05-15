package pw.binom.material

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.ExternalAnnotator
import com.intellij.lang.parameterInfo.ParameterInfoUtils
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiReferenceBase
import org.jetbrains.kotlin.idea.util.module
import org.jetbrains.kotlin.psi.psiUtil.startOffset
import pw.binom.ModuleHolder
import pw.binom.glsl.psi.GLSLASTWrapperPsiElement
import pw.binom.glsl.psi.GLSLCallMethodExp
import pw.binom.glsl.psi.GLSLFunctionDefinition
import pw.binom.glsl.psi.impl.GLSLAccessIdImpl
import pw.binom.glsl.psi.impl.GLSLCallMethodExpImpl
import pw.binom.glsl.psi.impl.GLSLFunctionDefinitionImpl
import pw.binom.material.compiler.CompileException
import pw.binom.material.compiler.Compiler
import pw.binom.material.compiler.MethodCallExpressionDesc
import pw.binom.material.compiler.SourceElement
import pw.binom.material.lex.Parser
import pw.binom.material.lex.ParserException
import java.io.StringReader
import com.intellij.openapi.module.Module as IdeModule

class ErrorAnnotator : ExternalAnnotator<ErrorAnnotator.Source, ErrorAnnotator.Result>() {
    class Source(
            val text: String,
            val file: VirtualFile,
            val module: IdeModule?,
            val project: Project?,
            val psiFile: PsiFile
    )

    sealed class Result {
        class ResultOK(val compiler: Compiler, val file: VirtualFile) : Result()
        class ResultException(val exception: Throwable) : Result()
    }

    override fun collectInformation(file: PsiFile, editor: Editor, hasErrors: Boolean): Source? =
            if (hasErrors)
                null
            else {
                Source(
                        text = editor.document.text,
                        file = file.virtualFile,
                        module = file.module,
                        project = editor.project,
                        psiFile = file
                )
            }

    override fun doAnnotate(collectedInfo: Source?): Result? {
        collectedInfo ?: return null
        return try {
            val holder = ModuleHolder.getInstance(collectedInfo.module!!)
            val module = SourceModule(holder.getRelativePath(collectedInfo.file))
            val parser = Parser(module, StringReader(collectedInfo.text))
            holder.resolver.getModule(collectedInfo.file)
            val compiler = Compiler(parser, module, holder.resolver)
            Result.ResultOK(compiler, collectedInfo.file)
        } catch (e: Throwable) {
            println("Exception->${e}")
            Result.ResultException(e)
        }
    }

    override fun apply(file: PsiFile, annotationResult: Result?, holder: AnnotationHolder) {
        annotationResult ?: return
        when (annotationResult) {
            is Result.ResultException -> {
                when (annotationResult.exception) {
                    is ParserException -> holder.createErrorAnnotation(TextRange.from(annotationResult.exception.position, annotationResult.exception.length), annotationResult.exception.message)
                    is CompileException -> holder.createErrorAnnotation(TextRange.from(annotationResult.exception.position, annotationResult.exception.length), annotationResult.exception.message)
                    else -> annotationResult.exception.printStackTrace()
                }
            }
            is Result.ResultOK -> {
                annotationResult.compiler.module.properties.asSequence()
                        .filter {
                            it.type == RootModule.sampler2DType
                        }
                        .forEach {
                            val prop = it.property!!
                            val value = prop.value
                            if (value == null) {
                                holder.createWarningAnnotation(it.source.asRange(), "Texture not set")
                            } else {
                                val textureFile = annotationResult.file.parent.findFileByRelativePath(value)
                                if (textureFile == null) {
                                    holder.createErrorAnnotation(it.source.asRange(), "Texture not found")
                                }
                            }

                        }
            }
        }

    }
}

fun SourcePoint.asRange() = TextRange.from(position, length)

class MM(val file: PsiFile, val ideModule: IdeModule) : MaterialVisiter {

    private val files = HashMap<String, PsiFile>()

    fun <T : GLSLASTWrapperPsiElement> findElement(v: SourceElement, type: Class<T>): T? {
        val module = (v.source.module as? SourceModule) ?: return null
        val psiFile = files.getOrPut(module.currentFilePath) {
            val file = ModuleHolder.getInstance(ideModule).findFileByRelativePath(module.currentFilePath)!!
            PsiManager.getInstance(ideModule.project).findFile(file)!!
        }
        return ParameterInfoUtils.findParentOfType(psiFile, v.source.position, type)
    }

    override fun visit(expression: MethodCallExpressionDesc) {
//        val call = file.findElementAt(expression.source.position) ?: return
//        val definition = file.findElementAt(expression.methodDesc.source.position) ?: return

        val call = ParameterInfoUtils.findParentOfType(file, expression.source.position, GLSLCallMethodExp::class.java) as GLSLCallMethodExpImpl? ?: return
        val definition = findElement(expression.methodDesc, GLSLFunctionDefinitionImpl::class.java)?:return//ParameterInfoUtils.findParentOfType(file, expression.methodDesc.source.position, GLSLFunctionDefinition::class.java) as GLSLFunctionDefinitionImpl

//        val callWrap = call.unwrapped!!.let { it as GLSLASTWrapperPsiElement }
//        val deffWrap = definition.unwrapped!!.let { it as GLSLASTWrapperPsiElement }

        val defName = definition.children.asSequence().mapNotNull { it as? GLSLAccessIdImpl }.first()
        val callName = call.children.asSequence().mapNotNull { it as? GLSLAccessIdImpl }.first()

        callName.internalReference = arrayOf(ReferenceToFunction(
                element = callName,
                rangeInElement = callName.textRangeInParent,
                ref = defName
        )
//                ReferenceToFunction(
//                        element = call,
//                        rangeInElement = call.texr,
//                        ref = definition
//                )
        )


        definition.internalReference = arrayOf(ReferenceToFunction(
                element = definition,
                rangeInElement = defName.textRangeInParent,
                ref = call
        ))

//        definition.internalReference = arrayOf(ReferenceToFunction(
//                element = defName,
//                rangeInElement = definition.textRangeInParent,
//                ref = call
//        ))

        println("call: ${call} ${call?.startOffset}   ${expression.source.position}")
        println("definition: $defName ${defName.textRange}")
    }
}

class ReferenceToFunction(element: PsiElement, val ref: PsiElement?, rangeInElement: TextRange) : PsiReferenceBase<PsiElement>(element, rangeInElement, false) {
    override fun resolve(): PsiElement? {
        return ref
    }

}

fun resolve(psiFile: PsiFile, module: SourceModule) {
    MM(psiFile, psiFile.module!!).visit(module)
}