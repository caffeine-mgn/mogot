package pw.binom.sceneEditor

import com.intellij.openapi.vfs.VirtualFile
import mogot.RenderContext
import mogot.gl.MaterialGLSL
import mogot.gl.Shader
import mogot.math.Matrix4fc
import pw.binom.material.compiler.Compiler
import pw.binom.material.generator.gles300.GLES300Generator
import pw.binom.material.psi.Parser
import java.io.StringReader

class ExternalMaterial(val view: SceneEditorView) : MaterialGLSL(view.gl) {
    private var _shader: Shader? = null
    var file: VirtualFile? = null
        set(value) {
            if (value == null) {
                compiler = null
                _shader = null
                field = value
            } else {
                val old = _shader
                if (old != null)
                    view.renderThread {
                        old.close()
                    }
                _shader = null
                value.inputStream.bufferedReader().use {
                    val text = it.readText()
                    println("Shader Text: $text")
                    val parser = Parser(StringReader(text))
                    compiler = Compiler(parser)
                }
                field = value
            }

        }
    var compiler: Compiler? = null
        private set


    override val shader: Shader
        get() = _shader!!

    override fun use(model: Matrix4fc, projection: Matrix4fc, renderContext: RenderContext) {
        if (_shader == null && compiler != null) {
            val gen = GLES300Generator(compiler!!)
            _shader = Shader(gl, gen.vp, gen.fp)
        }
        if (_shader == null) {
            view.default3DMaterial.use(model, projection, renderContext)
            return
        }
        super.use(model, projection, renderContext)
    }

    override fun unuse() {
        if (_shader == null) {
            view.default3DMaterial.unuse()
            return
        }
        super.unuse()
    }

    override fun close() {
        _shader?.close()
        _shader = null
        compiler = null
    }

}