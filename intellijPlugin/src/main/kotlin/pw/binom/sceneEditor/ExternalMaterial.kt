package pw.binom.sceneEditor

import com.intellij.openapi.vfs.VirtualFile
import mogot.Engine
import mogot.RenderContext
import mogot.Resources
import mogot.gl.MaterialGLSL
import mogot.gl.Shader
import mogot.math.Matrix4fc
import pw.binom.io.Closeable
import pw.binom.material.compiler.Compiler
import pw.binom.material.generator.gles300.GLES300Generator
import pw.binom.material.psi.Parser
import java.io.StringReader

class ExternalMaterial(engine: Engine, val file: VirtualFile) : MaterialGLSL(engine) {
    private var _shader: Shader? = null

    private fun load() = file.inputStream.bufferedReader().use {
        val text = it.readText()
        val parser = Parser(StringReader(text))
        Compiler(parser)
    }

    /*
    var file: VirtualFile? = null
        set(value) {
            if (value == null) {
                compiler = null
                _shader = null
                field = value
            } else {
                val old = _shader
                if (old != null)
                    engine.waitFrame {
                        old.close()
                    }
                _shader = null
                value.inputStream.bufferedReader().use {
                    val text = it.readText()
                    val parser = Parser(StringReader(text))
                    compiler = Compiler(parser)
                }
                field = value
            }

        }
    */
    var oldCompiler: Compiler? = null
    private var modificationStamp: Long? = null
    val compiler: Compiler
        get() {
            if (oldCompiler == null || modificationStamp == null || file.modificationStamp > modificationStamp ?: 0L) {
                modificationStamp = file.modificationStamp
                oldCompiler = load()
            }
            return load()
        }


    override val shader: Shader
        get() = _shader!!

    override fun use(model: Matrix4fc, projection: Matrix4fc, renderContext: RenderContext) {
        if (_shader == null && compiler != null) {
            val gen = GLES300Generator(compiler!!)
            _shader = Shader(engine.gl, gen.vp, gen.fp)
        }
        if (_shader == null) {
            return
        }
        super.use(model, projection, renderContext)
    }

    override fun unuse() {
        if (_shader == null) {
            return
        }
        super.unuse()
    }

    override fun dispose() {
        _shader?.close()
        _shader = null
        super.dispose()
    }

    fun instance() = MaterialInstance(this)
}

class ExternalManager(val engine: Engine): Closeable {
    private val files = HashMap<String, ExternalMaterial>()
    fun instance(file: VirtualFile): MaterialInstance {
        val material = files.getOrPut(file.path) { ExternalMaterial(engine, file) }
        material.disposeListener = {
            files.remove(file.path)
        }
        return material.instance()
    }

    override fun close() {
        files.values.forEach {
            while (!it.dec()){}
        }
    }
}

fun Resources.loadMaterial(file: VirtualFile): MaterialInstance {
    val manager = engine.manager("MaterialManager") { ExternalManager(engine) }
    return manager.instance(file)
}