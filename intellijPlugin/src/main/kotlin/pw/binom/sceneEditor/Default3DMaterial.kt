package pw.binom.sceneEditor

import mogot.Engine
import mogot.gl.MaterialGLSL
import mogot.gl.Shader
import pw.binom.material.compiler.Compiler
import pw.binom.material.generator.gles300.GLES300Generator
import pw.binom.material.psi.Parser
import java.io.StringReader

class Default3DMaterial(engine: Engine) : MaterialGLSL(engine) {

    override val shader: Shader = run {
        val text = """
@vertex
vec3 vertexPos
     
@normal
vec3 normalList

@projection
mat4 projection

@model
mat4 model

vec3 normal
            
vec4 vertex(){
    mat3 normalMatrix = mat3(transpose(inverse(model)))
    normal = vec3(normalMatrix * normalList)
    return vec4(projection * model * vec4(vertexPos, 1f))
}

vec4 fragment(vec4 color2){
    return vec4(1f,1f,1f,1f)
}

        """
        val compiler = Compiler(Parser(StringReader(text)))
        val gen = GLES300Generator.mix(listOf(compiler))
        Shader(engine.gl, gen.vp, gen.fp)
    }

    override fun dispose() {
        shader.close()
        super.dispose()
    }

}