package pw.binom.sceneEditor

import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import mogot.Engine
import mogot.Resources
import mogot.gl.MaterialGLSL
import mogot.gl.Shader
import mogot.math.Matrix4fc
import mogot.rendering.Display
import pw.binom.ModuleHolder
import pw.binom.io.Closeable
import pw.binom.material.SourceModule
import pw.binom.material.compiler.Compiler
import pw.binom.material.generator.gles300.GLES300Generator
import pw.binom.material.lex.Parser
import java.io.StringReader

val EDITOR_SHADER = """
    @property
    bool selected
    
    @property
    bool hover
    
    vec4 fragment(vec4 color){
        float cof = 0.1f
        if (selected)
           return vec4(color.x + cof,color.y + cof,color.z,color.w+0.1f)
        else
            if (hover)
                return vec4(color.x + cof,color.y + cof,color.z,color.w+0.1f)
            else
                return color
    } 
    
    vec4 vertex(){
        return vec4(0f,0f,0f,0f)
    }
"""

class ExternalMaterial(val engine: Engine, val project: Project, val file: VirtualFile) : MaterialGLSL(engine.gl) {
    private var _shader: Shader? = null

    private var lastModule: SourceModule? = null

    private fun load() = file.inputStream.bufferedReader().use {
        val text = it.readText()
        val module1 = SourceModule(file.path)
        val parser = Parser(module1, StringReader(text))
        val module = ModuleHolder.getInstance(ModuleUtil.findModuleForFile(file, project)!!)
        Compiler(parser, module1, module.resolver)
        lastModule = module1
        lastModule!!
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
    val compiler: SourceModule
        get() {
            return ModuleHolder.getInstance(file, project).resolver.getModule(file).checkValid()
//            if (oldCompiler == null || modificationStamp == null || file.modificationStamp > modificationStamp ?: 0L) {
//                modificationStamp = file.modificationStamp
//                oldCompiler = load()
//            }
//            return load()
        }


    override val shader: Shader
        get() = _shader!!

    private fun checkValid() {
        if (_shader == null) {
            val module = ModuleHolder.getInstance(ModuleUtil.findModuleForFile(file, project)!!)
            val mod = SourceModule(file.path)
            val c = Parser(mod, StringReader(EDITOR_SHADER)).let { Compiler(it, mod, module.resolver) }
            val gen = GLES300Generator.mix(listOf(compiler, mod))
            _shader = Shader(gl, gen.vp, gen.fp)
        }
    }

    override fun use(model: Matrix4fc, modelView: Matrix4fc, projection: Matrix4fc, context: Display.Context) {
        checkValid()
        if (_shader == null) {
            return
        }
        super.use(model, modelView, projection, context)
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

    fun instance() = MaterialInstance(engine, this)
}

class ExternalManager(val engine: Engine) : Closeable {
    private val files = HashMap<String, ExternalMaterial>()
    fun instance(project: Project, file: VirtualFile): MaterialInstance {
        val material = files.getOrPut(file.path) { ExternalMaterial(engine, project, file) }
        material.disposeListener = {
            files.remove(file.path)
        }
        return material.instance()
    }

    override fun close() {
        files.values.forEach {
            while (!it.dec()) {
            }
        }
    }
}

fun Resources.loadMaterial(project: Project, file: VirtualFile): MaterialInstance {
    val manager = engine.manager("MaterialManager") { ExternalManager(engine) }
    return manager.instance(project, file)
}