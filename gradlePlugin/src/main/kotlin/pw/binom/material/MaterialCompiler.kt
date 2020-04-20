package pw.binom.material

import mogot.material.MaterialLoader
import pw.binom.DesktopAssertTask
import pw.binom.asUTF8ByteArray
import pw.binom.io.wrap
import pw.binom.io.writeInt
import pw.binom.io.writeUTF8String
import pw.binom.material.compiler.Compiler
import pw.binom.material.generator.gles300.GLES300Generator
import pw.binom.material.psi.Parser
import java.io.File
import java.io.OutputStream
import java.io.StringReader

object MaterialCompiler {

    @JvmStatic
    fun compile(text: String, outputStream: OutputStream) {
        val parser = Parser(StringReader(text))
        val compiler = Compiler(parser)
        val shader = GLES300Generator.mix(listOf(compiler))
        //val generator = GLES300Generator(compiler)
        val out = outputStream.wrap()
        out.writeInt(2)
        out.writeUTF8String(MaterialLoader.GLES300_VP)
        val dataVp = shader.vp.asUTF8ByteArray()
        out.writeInt(dataVp.size)
        out.write(dataVp)
        out.writeUTF8String(MaterialLoader.GLES300_FP)
        val dataFp = shader.fp.asUTF8ByteArray()
        out.writeInt(dataFp.size)
        out.write(dataFp)
        out.flush()
    }

    @JvmStatic
    fun compile(file: File, outputStream: OutputStream) {
        val text = file.readText()
        compile(text, outputStream)
    }

    @JvmStatic
    fun compile(file: File, outputFile: File) {
        if (!DesktopAssertTask.isFileChanged(file, outputFile)) {
            println("$file: UP-TO-DATE")
            return
        }
        outputFile.outputStream().use {
            compile(file, it)
            println("$file: compiled")
        }
    }
}