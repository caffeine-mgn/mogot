package pw.binom.material.lex

import org.junit.Test
import pw.binom.material.EmptyModuleResolver
import pw.binom.material.SourceModule
import pw.binom.material.compiler.Compiler
import pw.binom.material.generator.gles300.GLES300Generator
import java.io.StringReader

class GLSL300ESTest {

    @Test
    fun incDecTest() {
        Parser(StringReader("""
        vec4 fragment(vec4 color){
            int b = 0
            b++
            return color
        }    
        """)).let { Compiler(it, SourceModule(), EmptyModuleResolver) }.let { GLES300Generator.mix(listOf(it)) }.let {
            it.fp
            it.vp
        }
    }

    @Test
    fun unarMinusDecTest() {
        Parser(StringReader("""
        vec4 fragment(vec4 color){
            int b = 0
            int c = -b            
            return color
        }    
        """)).let { Compiler(it, SourceModule(), EmptyModuleResolver) }.let { GLES300Generator.mix(listOf(it)) }.let {
            it.fp
            it.vp
        }
    }

    @Test
    fun parenthesisTest() {
        val compiler = Parser(StringReader("""
        vec4 fragment(vec4 color){
            int gg=(2+2)
        }
        """)).let { Compiler(it, SourceModule(), EmptyModuleResolver) }.let { GLES300Generator.mix(listOf(it)) }.let {
            println(it.fp)
            it.vp
        }
    }

    @Test
    fun orderTest() {
        val compiler = Parser(StringReader("""
        int clump2(){
            return 10            
        }
        vec4 fragment(vec4 color){
            int gg=(2+2) + clump2()
        }
        """)).let { Compiler(it, SourceModule(), EmptyModuleResolver) }.let { GLES300Generator.mix(listOf(it)) }.let {
            println(it.fp)
            it.vp
        }
    }
}