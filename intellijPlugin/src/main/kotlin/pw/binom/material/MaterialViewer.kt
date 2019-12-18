package pw.binom.material

import com.intellij.openapi.vfs.VirtualFile
import mogot.*
import mogot.gl.GL
import mogot.gl.MaterialGLSL
import mogot.gl.Shader
import mogot.math.Matrix4fc
import mogot.math.Vector3fc
import mogot.math.Vector3ic
import mogot.math.Vector4fc
import pw.binom.SimpleMaterial
import pw.binom.View3D
import pw.binom.io.Closeable
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

class MaterialViewer(val materialFileEditor: MaterialFileEditor) : View3D() {
    class TextureFile(val file: VirtualFile) : Closeable {
        private var texture: Texture2D? = null
        fun getTexture2D(engine: Engine): Texture2D {
            if (texture == null) {
                texture = engine.resources.syncCreateTexture2D(file.path)
                texture!!.inc()
            }
            return texture!!
        }

        override fun close() {
            texture?.dec()
        }
    }

    lateinit var material: DynamicMaterialGLSL

    private var vp = ""
    private var fp = ""
    private var vpOld = ""
    private var fpOld = ""

    private val params = HashMap<String, Any>()

    fun set(name: String, value: Any?) {
        if (value == null) {
            val oldValue = params.remove(name)
            if (oldValue is TextureFile) {
                engine.waitFrame {
                    oldValue.close()
                }
            }
        } else {
            val old = params.remove(name)
            if (old is TextureFile && value is TextureFile) {
                if (old.file.parent == value.file) {
                    params[name] = old
                    return
                }
            }
            if (old is TextureFile)
                engine.waitFrame {
                    old.close()
                }
            params[name] = value
        }
    }

    inner class DynamicMaterialGLSL(engine: Engine) : MaterialGLSL(engine) {
        private val standart = SimpleMaterial(engine)

        init {
            standart.inc()
        }

        var dynamicShader: Shader? = null
        override val shader: Shader
            get() = dynamicShader ?: standart.shader

        override fun dispose() {
            standart.dec()
            dynamicShader?.close()
            super.dispose()
        }

        override fun use(model: Matrix4fc, projection: Matrix4fc, renderContext: RenderContext) {
            super.use(model, projection, renderContext)
            params.forEach { (name, value) ->
                println("$name: $value")
            }
            params.forEach { (name, value) ->
                when (value) {
                    is Int -> shader.uniform(name, value)
                    is Float -> shader.uniform(name, value)
                    is Vector3fc -> shader.uniform(name, value)
                    is Vector3ic -> shader.uniform(name, value)
                    is Vector4fc -> shader.uniform(name, value)
                    is TextureFile -> {
                        gl.activeTexture(gl.TEXTURE0)
                        gl.bindTexture(gl.TEXTURE_2D, value.getTexture2D(engine).gl)
                        shader.uniform(name, 0)
                    }
                    else -> throw IllegalStateException("Unknown uniform type ${value::class.java.name}")
                }
            }
        }
    }

//    private val fpPrefix = """#version 300 es
//
//#ifdef GL_ES
//precision mediump float;
//#endif
//"""

    override fun render() {
        if (vp != vpOld || fp != fpOld) {
            vpOld = vp
            fpOld = fp
            try {
                material.dynamicShader?.close()
                material.dynamicShader = Shader(gl, vp, fp)
            } catch (e: Throwable) {
                println("MaterialView ERROR: ${e.message}")
                println("fp:\n$fp")
                material.dynamicShader = null
            }
        }
        super.render()
    }


    fun setShader(vp: String, fp: String) {
        this.vp = vp
        this.fp = fp
        repaint()
    }

    private val initListeners = ArrayList<() -> Unit>()

    fun addInitListener(func: () -> Unit) {
        initListeners += func
    }

    private val renderListeners = ArrayList<() -> Unit>()

    fun addRenderListener(func: () -> Unit) {
        renderListeners += func
    }

    override fun init() {
        super.init()
        initListeners.forEach {
            it()
        }
        material = DynamicMaterialGLSL(engine)
        val node = GeomNode2().also {
            it.geom = Geoms.buildCube2(gl, 1f)
            it.material = material
        }
        root!!.addChild(node)

        val l = OmniLight()
        val node1 = GeomNode().apply {
            geom = Geoms.solidSphere(gl, 1f, 30, 30)
            material = SimpleMaterial(engine)
        }
        node1.position.z = -30f
        node1.position.x = 30f
        node1.position.y = 30f
        node1.addChild(l)
        root!!.addChild(node1)
        resetCam()
    }

    override fun dispose() {
        params.values.asSequence()
                .mapNotNull { it as? TextureFile }
                .forEach { it.close() }
        fpOld = ""
        vpOld = ""
        material.dynamicShader?.close()
        root!!.childs.toTypedArray().forEach {
            if (it !is Camera)
                it.parent = null
        }
    }

    private inner class GeomNode2 : VisualInstance(), MaterialNode by MaterialNodeImpl() {
        var geom: Geom3D2? = null
        override fun render(model: Matrix4fc, projection: Matrix4fc, renderContext: RenderContext) {
            material?.use(model, projection, renderContext)
            renderListeners.forEach {
                it()
            }
            geom?.draw()
            material?.unuse()
        }

        override fun close() {
            geom?.close()
            material = null
        }
    }
}
