package pw.binom.material

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.ExternalAnnotator
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import pw.binom.material.compiler.CompileException
import pw.binom.material.compiler.Compiler
import pw.binom.material.compiler.SingleType
import pw.binom.material.psi.Parser
import pw.binom.material.psi.ParserException
import java.io.StringReader

class ErrorAnnotator : ExternalAnnotator<ErrorAnnotator.Source, ErrorAnnotator.Result>() {
    class Source(val text: String, val file: VirtualFile)
    sealed class Result {
        class ResultOK(val compiler: Compiler, val file: VirtualFile) : Result()
        class ResultException(val exception: Throwable) : Result()
    }

    override fun collectInformation(file: PsiFile, editor: Editor, hasErrors: Boolean): Source? =
            if (hasErrors)
                null
            else
                Source(editor.document.text, file.virtualFile)

    override fun doAnnotate(collectedInfo: Source?): Result? {
        collectedInfo ?: return null
        return try {
            val parser = Parser(StringReader(collectedInfo.text))
            val compiler = Compiler(parser)
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
                val sampler2D = annotationResult.compiler.properties.mapNotNull {
                    val type = it.key.type as? SingleType
                    type ?: return@mapNotNull null
                    if (type.clazz.name != "sampler2D")
                        return@mapNotNull null
                    it.key to it.value["value"]?.takeIf { it.isNotBlank() }
                }
                sampler2D.forEach {
                    if (it.second == null) {
                        holder.createWarningAnnotation(TextRange.from(it.first.source.position, it.first.source.length), "Texture not set")
                    } else {
                        val textureFile = annotationResult.file.parent.findFileByRelativePath(it.second!!)
                        if (textureFile == null) {
                            holder.createErrorAnnotation(TextRange.from(it.first.source.position, it.first.source.length), "Texture not found")
                        }
                    }

                }
            }
        }

    }
}