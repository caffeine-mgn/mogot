package pw.binom.material

import pw.binom.io.OutputStream
import pw.binom.io.file.FileNotFoundException
import pw.binom.io.utf8Appendable
import pw.binom.material.compiler.Compiler
import pw.binom.material.generator.gles300.GLES300Generator
import pw.binom.material.psi.Parser
import java.io.StringReader

object MaterialFile {
    fun parse(resolver: PathResolver, file: String): Parser {
        val material = resolver.getFile(file) ?: throw FileNotFoundException("Can't find material file \"$file\"")
        return Parser(StringReader(material.getSource()))
    }

    fun gen(parser: Parser, gles3: Boolean, stream: OutputStream) {
        if (gles3) {
            val sb = stream.utf8Appendable()
            val compiler = Compiler(parser)
            val generator = GLES300Generator(compiler)
            sb.append("gles3_fp=").append(generator.fp)
            sb.append("gles3_vp=").append(generator.vp)
            stream.flush()
        }
    }
}