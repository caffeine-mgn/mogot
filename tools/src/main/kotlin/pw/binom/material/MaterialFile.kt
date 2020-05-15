package pw.binom.material

/*
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
*/