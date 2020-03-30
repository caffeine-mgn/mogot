package pw.binom.material

import mogot.*
import mogot.gl.GL
import mogot.gl.MaterialGLSL
import mogot.gl.Shader
import mogot.math.Matrix4fc
import mogot.math.Vector3fc
import mogot.math.Vector3ic
import mogot.math.Vector4fc
import mogot.rendering.Display
import pw.binom.SimpleMaterial
import pw.binom.View3D
import pw.binom.sceneEditor.ExternalTexture
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

class MaterialViewer(val materialFileEditor: MaterialFileEditor) : View3D() {

    lateinit var material: DynamicMaterialGLSL

    private var vp = ""
    private var fp = ""
    private var vpOld = ""
    private var fpOld = ""

    private val params = HashMap<String, Any>()

    private val activeTextures = ArrayList<ExternalTexture>()
    private val textureId = HashMap<ExternalTexture, Int>()

    var ExternalTexture.id: Int
        get() = textureId[this] ?: -1
        set(value) {
            textureId[this] = value
        }

    private fun updateTextureIndexes() {
        activeTextures.forEachIndexed { index, textureFile ->
            textureFile.id = index
        }
    }

    fun set(name: String, value: Any?) {
        val oldValue = params.remove(name)
        if (oldValue is ExternalTexture) {
            activeTextures.remove(oldValue)
            textureId.remove(oldValue)
            oldValue.dec()
        }
        if (value is ExternalTexture) {
            activeTextures.add(value)
            value.inc()
        }
        if (value != null)
            params[name] = value
        updateTextureIndexes()
    }


    inner class DynamicMaterialGLSL(gl: GL) : MaterialGLSL(gl) {
        private val standart = SimpleMaterial(gl)

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

        override fun use(model: Matrix4fc, projection: Matrix4fc, context: Display.Context) {
            super.use(model, projection, context)
            params.forEach { (name, value) ->
                when (value) {
                    is Int -> shader.uniform(name, value)
                    is Float -> shader.uniform(name, value)
                    is Vector3fc -> shader.uniform(name, value)
                    is Vector3ic -> shader.uniform(name, value)
                    is Vector4fc -> shader.uniform(name, value)
                    is ExternalTexture -> {
                        gl.activeTexture(gl.TEXTURE0 + value.id)
                        gl.bindTexture(gl.TEXTURE_2D, value.gl.gl)
                        shader.uniform(name, value.id)
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
                println("fp:\n$fp")
            } catch (e: Throwable) {
                println("MaterialView ERROR: ${e.message}")
                println("fp:")
                fp.lines().forEachIndexed { index, s ->
                    println("$index -> $s")
                }
                e.printStackTrace()
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
        root = Node()
        material = DynamicMaterialGLSL(engine.gl)
        val node = GeomNode2().also {
            it.geom.value = Geoms.buildCube2(gl, 1f)
            it.material.value = material
        }
        root!!.addChild(node)

        val l = PointLight()
        val node1 = GeomNode().apply {
            geom.value = Geoms.solidSphere(gl, 1f, 30, 30)
            material.value = SimpleMaterial(engine.gl)
        }
        node1.position.z = -30f
        node1.position.x = 30f
        node1.position.y = 30f
        node1.addChild(l)
        root!!.addChild(node1)
        resetCam()
    }

    override fun dispose() {
        activeTextures.forEach {
            it.dec()
        }
        activeTextures.clear()
        textureId.clear()
        params.clear()
        fpOld = ""
        vpOld = ""
        material.dynamicShader?.close()
        root!!.childs.toTypedArray().forEach {
            if (it !is Camera)
                it.parent = null
        }
    }

    private inner class GeomNode2 : VisualInstance(), MaterialNode by MaterialNodeImpl() {
        var geom = ResourceHolder<Geom3D2>()
        override fun render(model: Matrix4fc, projection: Matrix4fc, context: Display.Context) {
            material.value?.use(model, projection, context)
            renderListeners.forEach {
                it()
            }
            geom.value?.draw()
            material.value?.unuse()
        }

        override fun close() {
            geom.dispose()
            material.dispose()
        }
    }
}
