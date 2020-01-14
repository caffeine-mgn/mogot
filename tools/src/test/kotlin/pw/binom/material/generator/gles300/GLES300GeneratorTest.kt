package pw.binom.material.generator.gles300

import org.junit.Test
import pw.binom.material.compiler.Compiler
import pw.binom.material.psi.Parser
import java.io.StringReader

class GLES300GeneratorTest {

    @Test
    fun mixTest() {
        val shader1 = """
            
            vec4 aa
            
            @vertex
            vec3 vertexPos
            
            vec4 genFragment1(){
                return vec4(vertexPos.x,1f,1f,1f) 
            }
            vec4 fragment(vec4 color){
                return genFragment1() + aa
            }
            
            vec4 vertex(){
                aa = vec4(1f,0f,0f,1f)
                return vec4(vertexPos.x,vertexPos.y,vertexPos.z, 1f)
            }
        """

        val shader2 = """
            
            @vertex
            vec3 vertexPos
            
            vec4 genFragment2(){
                return vec4(vertexPos.x,0f,0f,0f) 
            }
            vec4 fragment(vec4 color){
                return genFragment2()
            }
            
            vec4 vertex(){
                return vec4(vertexPos.x,vertexPos.y,vertexPos.z, 1f)
            }
        """

        val sh1 = Parser(StringReader(shader1)).let { Compiler(it) }
        val sh2 = Parser(StringReader(shader2)).let { Compiler(it) }

        val result = GLES300Generator.mix(listOf(sh1, sh2))
        println("FP:\n${result.fp}")
        println("\n----------\n")
        println("VP:\n${result.vp}")
    }
}