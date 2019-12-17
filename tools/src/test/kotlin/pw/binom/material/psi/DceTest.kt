package pw.binom.material.psi

import org.junit.Test
import pw.binom.material.DCE
import pw.binom.material.compiler.Compiler
import java.io.StringReader
import kotlin.test.assertNotNull

class DceTest {

    @Test
    fun testInCall() {
        val compiler = Parser(StringReader("""
        @uv
        vec2 vertexUV
        
        vec3 normal
        
        @property
        sampler2D image            
        
        vec4 fragment(vec4 color2){
            vec4 tex = texture(image,vertexUV).rgba
            return vec4(0f,0f,0f,0f)
        }
        """)).let { Compiler(it) }

        val dce = DCE(compiler)
        assertNotNull(dce.fieldsFP.find { it.name == "image" })
        assertNotNull(dce.fieldsFP.find { it.name == "vertexUV" })
    }
}