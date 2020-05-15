package pw.binom.material.lex

import pw.binom.checkNext
import java.io.Reader
import pw.binom.material.Module
import pw.binom.material.SourceModule

class Parser(module: SourceModule, reader: Reader) {

    open class StringElement(module: Module, val string: String, element: TokenType, text: String, line: Int, column: Int, position: Int) : LexStream.Element<TokenType>(module, element, text, line, column, position)

    private val stream = LexStream(module) {
        lexer.next()?.let {
            if (it == TokenType.STRING)
                StringElement(module, lexer.stringLiteral(), it, lexer.text, lexer.line, lexer.column, lexer.position)
            else
                LexStream.Element(module, it, lexer.text, lexer.line, lexer.column, lexer.position)
        }
    }
    private val lexer = pw.binom.material.lex.GLSLLexer(reader)

//    val imports = ArrayList<String>()

    private val _global = ArrayList<Global>()
    val global: List<Global>
        get() = _global

    //val properties = HashMap<GlobalVar, Map<String, String>>()
    var model: GlobalVar? = null
    var modelView: GlobalVar? = null
    var uv: GlobalVar? = null
    var normal: GlobalVar? = null
    var projection: GlobalVar? = null
    var camera: GlobalVar? = null
    var vertex: GlobalVar? = null

    init {
        while (true) {
            val b = Global.read(stream) ?: break
            _global += b
        }

        val next = stream.checkNext(true)
        if (next != null) {
            throw ParserException("Unknown token \"${next.text}\" line: ${next.line} column: ${next.column}", next.position, next.text.length)
        }
/*
        val it = _global.listIterator()
        while (it.hasNext()) {
            val e = it.next()
            if (e is Annotation) {
                when (e.name) {
                    "property" -> {
                        it.remove()
                        properties[it.next() as GlobalVar] = e.properties
                    }
                    "vertex" -> {
                        it.remove()
                        vertex = it.next() as GlobalVar
                    }
                    "normal" -> {
                        it.remove()
                        normal = it.next() as GlobalVar
                    }
                    "uv" -> {
                        it.remove()
                        uv = it.next() as GlobalVar
                    }
                    "modelView" -> {
                        it.remove()
                        modelView = it.next() as GlobalVar
                    }
                    "model" -> {
                        it.remove()
                        model = it.next() as GlobalVar
                    }
                    "projection" -> {
                        it.remove()
                        projection = it.next() as GlobalVar
                    }
                    "cameraPosition" -> {
                        it.remove()
                        camera = it.next() as GlobalVar
                    }
                    "import" -> {
                        it.remove()
                        val file = e.properties["file"]
                                ?: throw ParserException("File not set. Line: ${e.line} column: ${e.column}", e.position, e.text.length)
                        imports += file
                    }
                    else -> throw ParserException("Unknown annotation \"${e.name}\" line: ${e.line} column: ${e.column}", e.position, e.text.length)
                }
            }
        }
        */
    }
}