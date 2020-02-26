package pw.binom.sceneEditor

import mogot.Engine
import mogot.Material
import mogot.RenderContext
import mogot.ResourceImpl
import mogot.gl.MaterialGLSL
import mogot.gl.Shader
import mogot.math.Matrix4fc
import mogot.math.Vector4f
import mogot.math.Vector4fc
import pw.binom.material.compiler.Compiler
import pw.binom.material.generator.gles300.GLES300Generator
import pw.binom.material.psi.Parser
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
    override var reservedTexturesMaxId: Int = 0

    override fun use(model: Matrix4fc, projection: Matrix4fc, renderContext: RenderContext) {
        root.use(model, projection, renderContext)
        root.shader.uniform("selected", selected)
        root.shader.uniform("hover", hover)
        root.shader.uniform("color", color)
    }

    override fun unuse() {
        root.unuse()
    }

}

class Default3DMaterial(engine: Engine) : MaterialGLSL(engine) {

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

@model
mat4 model

vec3 normal
            
vec4 vertex(){
    mat3 normalMatrix = mat3(transpose(inverse(model)))
    normal = vec3(normalMatrix * normalList)
    return vec4(projection * model * vec4(vertexPos, 1f))
}

vec4 fragment(vec4 color2){
    return color
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