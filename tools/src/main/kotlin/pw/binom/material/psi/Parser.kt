package pw.binom.material.psi

import pw.binom.checkNext
import java.io.Reader

class Parser(reader: Reader) {

    open class StringElement(val string: String, element: TokenType, text: String, line: Int, column: Int, position: Int) : LexStream.Element<TokenType>(element, text, line, column, position)

    private val stream = LexStream {
        lexer.next()?.let {
            if (it == TokenType.STRING)
                StringElement(lexer.stringLiteral(), it, lexer.text, lexer.line, lexer.column, lexer.position)
            else
                LexStream.Element(it, lexer.text, lexer.line, lexer.column, lexer.position)
        }
    }
    private val lexer = GLSLLexer(reader)

    private val _global = ArrayList<Global>()
    val global: List<Global>
        get() = _global
    val properties = HashMap<GlobalVar, Property>()
    var model: GlobalVar? = null
    var uv: GlobalVar? = null
    var normal: GlobalVar? = null
    var projection: GlobalVar? = null
    var vertex: GlobalVar? = null

    init {
        while (true) {
            val b = Global.read(stream) ?: break
            _global += b
        }

        val next = stream.checkNext(true)
        if (next != null) {
            throw ParserException("Unknown token \"${next.text}\"", next.position, next.text.length)
        }

        val it = _global.listIterator()
        while (it.hasNext()) {
            when (val e = it.next()) {
                is Property -> {
                    it.remove()
                    properties[it.next() as GlobalVar] = e
                }
                is Model -> {
                    it.remove()
                    model = it.next() as GlobalVar
                }
                is Uv -> {
                    it.remove()
                    uv = it.next() as GlobalVar
                }
                is Projection -> {
                    it.remove()
                    projection = it.next() as GlobalVar
                }
                is Normal -> {
                    it.remove()
                    normal = it.next() as GlobalVar
                }
                is Vertex -> {
                    it.remove()
                    vertex = it.next() as GlobalVar
                }
            }
        }
    }
}