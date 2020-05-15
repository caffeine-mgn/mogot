package pw.binom.material.lex

import org.junit.Test
import pw.binom.material.EmptyModuleResolver
import pw.binom.material.SourceModule
import pw.binom.material.compiler.Compiler
import java.io.StringReader

class CompileTest {

    @Test
    fun incDecTest() {
        val compiler = Parser(StringReader("""
        bool test(vec4 color2){
            int gg=10
            int b = gg++
        }
        """)).let { Compiler(it, SourceModule(), EmptyModuleResolver) }
    }

    @Test
    fun inverTest() {
        val compiler = Parser(StringReader("""
        bool test(vec4 color2){
            int gg=10
            int b = -gg
        }
        """)).let { Compiler(it, SourceModule(), EmptyModuleResolver) }
    }

    @Test
    fun parenthesisTest() {
        val compiler = Parser(StringReader("""
        bool test(vec4 color2){
            int gg=(2+2)
        }
        """)).let { Compiler(it, SourceModule(), EmptyModuleResolver) }
    }

    @Test
    fun constGlobalField() {
        val compiler = Parser(StringReader("""
        float PI = 3.14159265359f
        """)).let { Compiler(it, SourceModule(), EmptyModuleResolver) }
    }

    @Test
    fun testNull() {
        val compiler = Parser(StringReader("""
            @property
            sampler2D texture
            vec4 vertex(){
                bool tex=false
                if (texture!=null){
                    tex=true
                }
                return vec4(1f,1f,1f,1f)
            }
        """)).let { Compiler(it, SourceModule(), EmptyModuleResolver) }
    }

    @Test
    fun annotationTest(){
        val parser = Parser(StringReader("""
            @test(value="123")
        """))

        parser.global.size.eq(1)

        val ann = parser.global[0] as Annotation

        ann.name.eq("test")
        ann.properties.size.eq(1)
        ann.properties.first().also {
            it.key.eq("value")
            it.value.eq("123")
        }

    }
}