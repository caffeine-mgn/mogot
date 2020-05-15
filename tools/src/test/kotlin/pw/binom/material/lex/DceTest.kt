package pw.binom.material.lex

import org.junit.Test
import pw.binom.material.DCE
import pw.binom.material.EmptyModuleResolver
import pw.binom.material.SourceModule
import pw.binom.material.compiler.Compiler
import java.io.StringReader

class DceTest {

    @Test
    fun testInCall() {
        val compiler = Parser(StringReader("""
        @uv
        vec2 vertexUV
        
        vec3 normal
        vec3 normal2
        
        @property
        sampler2D image            
        
        vec4 fff(){
            return vec4(normal2.x,normal2.y,normal2.z, 1f)
        }
        
        vec4 fragment(vec4 color2){
            vec4 tex = texture(image,vertexUV).rgba
            return vec4(0f,fff().x,0f,0f)
        }
        """)).let { Compiler(it, SourceModule(), EmptyModuleResolver) }

        val dce = DCE(compiler.module)
        dce.fieldsFP.find { it.name == "image" }.notNull()
        dce.fieldsFP.find { it.name == "vertexUV" }.notNull()
        dce.methodsFP.find { it.name == "fff" }.notNull()
        dce.fieldsFP.find { it.name == "normal2" }.notNull()
    }
}