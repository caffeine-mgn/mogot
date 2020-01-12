package pw.binom

import pw.binom.material.compiler.Compiler
import pw.binom.material.generator.gles300.GLES300Generator
import pw.binom.material.psi.LexStream
import pw.binom.material.psi.Parser
import pw.binom.material.psi.TokenType
import java.io.StringReader

fun LexStream<TokenType>.checkNext(skipEndLine: Boolean = false): LexStream.Element<TokenType>? {
    var r: LexStream.Element<TokenType>? = null
    safe {
        r = skipSpace(skipEndLine)
        null
    }
    return r
}


fun LexStream<TokenType>.skipSpace(skipEndLine: Boolean = false): LexStream.Element<TokenType>? {
    do {
        val l = next() ?: return null
        if (l.element != TokenType.WHITE_SPACE && (!skipEndLine || l.element != TokenType.END_LINE))
            return l
    } while (true)
}

fun LexStream.Element<TokenType>.ifType(token: TokenType): LexStream.Element<TokenType>? {
    if (element == token)
        return this
    return null
}

object Test {
    @JvmStatic
    fun main(args: Array<String>) {
        val program = """
            
            class Light {
                vec3 position
                vec3 diffuse
                float specular
                
                bool doit(){
                    return true
                }
            }
            @property(min=0, max=1, hidden=true)
            Light lights[10]
            
            @property(min=0, max=1)
            float specular
            
            
            
            @vertex
            vec3 vertexPos
            
            @normal
            vec3 normalList
            
            @uv
            vec2 vertexUV
            
            @projection
            mat4 projection
            
            @model
            mat4 model
            
            vec3 vVertex
            vec3 normal
            
            vec3 vertex(){
                mat3 normalMatrix = mat3(transpose(inverse(model)))
                normal = vec3(normalMatrix * normalList)
                vVertex = vec3(model * vec4(vertexPos, 1f))
                return vec3(projection * model * vec4(vertexPos, 1f))
            }
            
            vec4 fragment(vec4 color2){
                vec3 ff = normal
                for (int i=0;i<lights.size; i=i+1){
                    lights[i].doit()
                    ff = lights[i].diffuse
                }
                return color2 * specular
            }
        """.trimIndent()
        val p = Parser(StringReader(program))
        val compiler = Compiler(p)
        val vv = GLES300Generator.mix(listOf(compiler))

        println("vp:\n${vv.vp}\n---------------------\nfp:\n${vv.fp}")
//        val parser = GLSLParser()
//        val root = ShaderTokenType("")
//        parser.parse(root, Builder())

//        val l = GLSLLexer(StringReader(program))
//
//
//
//        while (true) {
//            val b = l.advance()
//
//            if (b == null) {
//                println()
//                return
//            }
//
//
//            println("->$b")
//        }
    }
}