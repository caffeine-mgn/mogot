package pw.binom.material

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.ExternalAnnotator
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import pw.binom.material.compiler.CompileException
import pw.binom.material.compiler.Compiler
import pw.binom.material.psi.Parser
import pw.binom.material.psi.ParserException
import java.io.StringReader

class ErrorAnnotator : ExternalAnnotator<String, Throwable?>() {
    override fun collectInformation(file: PsiFile, editor: Editor, hasErrors: Boolean): String? =
            if (hasErrors)
                null
            else
                editor.document.text

    override fun doAnnotate(collectedInfo: String?): Throwable? {
        return try {
            val parser = Parser(StringReader(collectedInfo!!))
            Compiler(parser)
            println("OK")
            null
        } catch (e: Throwable) {
            println("Exception->${e}")
            e
        }
    }

    override fun apply(file: PsiFile, annotationResult: Throwable?, holder: AnnotationHolder) {
        annotationResult ?: return
        when (annotationResult) {
            is ParserException -> holder.createErrorAnnotation(TextRange.from(annotationResult.position, annotationResult.length), annotationResult.message)
            is CompileException -> holder.createErrorAnnotation(TextRange.from(annotationResult.position, annotationResult.length), annotationResult.message)
            else -> annotationResult.printStackTrace()
        }
    }
}