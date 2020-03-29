package pw.binom.sceneEditor

import mogot.*
import mogot.gl.GL
import mogot.gl.MaterialGLSL
import mogot.gl.Shader
import mogot.math.Matrix4fc
import mogot.rendering.Display
import pw.binom.material.compiler.Compiler
import pw.binom.material.generator.gles300.GLES300Generator
import pw.binom.material.psi.Parser
import java.io.StringReader
import pw.binom.material.Default2DMaterial as ToolsDefault2DMaterial

class Material2DInstance(val root: Default2DMaterial) : EditableMaterial, ResourceImpl() {

    override var selected = false
    override var hover = false
    var image by ResourceHolder<ExternalTextureFS>()


    init {
        root.inc()
    }

    override fun dispose() {
        root.dec()
        super.dispose()
    }

    override fun use(model: Matrix4fc, projection: Matrix4fc, context: Display.Context) {
        root.use(model, projection, context)
        root.shader.uniform("selected", selected)
        root.shader.uniform("hover", hover)
        if (image != null) {
            root.gl.activeTexture(root.gl.TEXTURE0)
            root.gl.bindTexture(root.gl.TEXTURE_2D, image!!.gl.gl)
            root.shader.uniform(ToolsDefault2DMaterial.IMAGE_PROPERTY, 0)
        } else {
            root.gl.activeTexture(root.gl.TEXTURE0)
            root.gl.bindTexture(root.gl.TEXTURE_2D, null)
            root.shader.uniform(ToolsDefault2DMaterial.IMAGE_PROPERTY, 0)
        }
    }

    override fun unuse() {
        root.unuse()
    }

}

class Default2DMaterial(gl: GL) : MaterialGLSL(gl) {

    fun instance() = Material2DInstance(this)

    override val shader: Shader = run {
        val text = ToolsDefault2DMaterial.SOURCE
        val c = Parser(StringReader(EDITOR_SHADER)).let { Compiler(it) }

        val compiler = Compiler(Parser(StringReader(text)))
        val gen = GLES300Generator.mix(listOf(compiler, c))
        Shader(gl, gen.vp, gen.fp)
    }

    override fun dispose() {
        shader.close()
        super.dispose()
    }

}