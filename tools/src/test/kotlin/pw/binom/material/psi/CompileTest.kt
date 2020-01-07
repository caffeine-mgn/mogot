package pw.binom.material.psi

import org.junit.Test
import pw.binom.material.DCE
import pw.binom.material.compiler.Compiler
import java.io.StringReader
import kotlin.test.assertNotNull

class CompileTest {

    @Test
    fun incDecTest(){
        val compiler = Parser(StringReader("""
        bool test(vec4 color2){
            int gg=10
            int b = gg++
        }
        """)).let { Compiler(it) }
    }

    @Test
    fun inverTest(){
        val compiler = Parser(StringReader("""
        bool test(vec4 color2){
            int gg=10
            int b = -gg
        }
        """)).let { Compiler(it) }
    }

    @Test
    fun parenthesisTest(){
        val compiler = Parser(StringReader("""
        bool test(vec4 color2){
            int gg=(2+2)
        }
        """)).let { Compiler(it) }
    }
}