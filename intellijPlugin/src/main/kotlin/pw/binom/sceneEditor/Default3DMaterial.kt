package pw.binom.sceneEditor

import mogot.ResourceImpl
import mogot.gl.GL
import mogot.gl.MaterialGLSL
import mogot.gl.Shader
import mogot.math.Matrix4fc
import mogot.math.Vector4f
import mogot.math.Vector4fc
import mogot.rendering.Display
import pw.binom.material.EmptyModuleResolver
import pw.binom.material.SourceModule
import pw.binom.material.compiler.Compiler
import pw.binom.material.generator.gles300.GLES300Generator
import pw.binom.material.lex.Parser
import java.io.StringReader

class MInstance(val root: MaterialGLSL, color: Vector4fc) : EditableMaterial, ResourceImpl() {
    val color = Vector4f(color)

    init {
        root.inc()
    }

    override fun dispose() {
        root.dec()
        super.dispose()
    }

    override var hover: Boolean = false
    override var selected: Boolean = false

    override fun use(model: Matrix4fc, modelView: Matrix4fc, projection: Matrix4fc, context: Display.Context) {
        root.use(model, modelView, projection, context)
        root.shader.uniform("selected", selected)
        root.shader.uniform("hover", hover)
        root.shader.uniform("color", color)
    }

    override fun unuse() {
        root.unuse()
    }

}

class Default3DMaterial(gl: GL) : MaterialGLSL(gl) {

    fun instance(color: Vector4fc) = MInstance(this, color)

    override val shader: Shader = run {
        val text = """
@vertex
vec3 vertexPos
     
@normal
vec3 normalList

@projection
mat4 projection

@property
vec4 color

@modelView
mat4 modelView

vec3 normal
            
vec4 vertex(){
    mat3 normalMatrix = mat3(transpose(inverse(modelView)))
    normal = vec3(normalMatrix * normalList)
    return vec4(projection * modelView * vec4(vertexPos, 1f))
}

vec4 fragment(vec4 color2){
    return color
}

        """
        val mod = SourceModule("")
        val compiler = Compiler(Parser(mod, StringReader(text)), mod, EmptyModuleResolver)
        val gen = GLES300Generator.mix(listOf(mod))
        Shader(gl, gen.vp, gen.fp)
    }

    override fun dispose() {
        shader.close()
        super.dispose()
    }

}