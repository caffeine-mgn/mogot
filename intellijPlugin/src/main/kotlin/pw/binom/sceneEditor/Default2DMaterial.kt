package pw.binom.sceneEditor

import mogot.*
import mogot.gl.MaterialGLSL
import mogot.gl.Shader
import mogot.math.Matrix4fc
import mogot.math.Vector4fc
import pw.binom.material.compiler.Compiler
import pw.binom.material.generator.gles300.GLES300Generator
import pw.binom.material.psi.Parser
import java.io.StringReader
import pw.binom.material.Default2DMaterial as ToolsDefault2DMaterial

class Material2DInstance(val root: Default2DMaterial) : EditableMaterial, ResourceImpl() {

    override var selected = false
    override var reservedTexturesMaxId: Int = 0
    override var hover = false
    var image by ResourceHolder<ExternalTextureFS>()


    init {
        root.inc()
    }

    override fun dispose() {
        root.dec()
        super.dispose()
    }

    override fun use(model: Matrix4fc, projection: Matrix4fc, renderContext: RenderContext) {
        root.use(model, projection, renderContext)
        root.shader.uniform("selected", selected)
        root.shader.uniform("hover", hover)
        if (image != null) {
            root.engine.gl.activeTexture(root.engine.gl.TEXTURE0)
            root.engine.gl.bindTexture(root.engine.gl.TEXTURE_2D, image!!.gl.glTexture)
            root.shader.uniform(ToolsDefault2DMaterial.IMAGE_PROPERTY, 0)
        } else {
            root.engine.gl.activeTexture(root.engine.gl.TEXTURE0)
            root.engine.gl.bindTexture(root.engine.gl.TEXTURE_2D, null)
            root.shader.uniform(ToolsDefault2DMaterial.IMAGE_PROPERTY, 0)
        }
    }

    override fun unuse() {
        root.unuse()
    }

}

class Default2DMaterial(engine: Engine) : MaterialGLSL(engine) {

    fun instance() = Material2DInstance(this)

    override val shader: Shader = run {
        val text = ToolsDefault2DMaterial.SOURCE
        val c = Parser(StringReader(EDITOR_SHADER)).let { Compiler(it) }

        val compiler = Compiler(Parser(StringReader(text)))
        val gen = GLES300Generator.mix(listOf(compiler, c))
        Shader(engine.gl, gen.vp, gen.fp)
    }

    override fun dispose() {
        shader.close()
        super.dispose()
    }

}