package pw.binom

import mogot.*
import mogot.gl.GL
import mogot.gl.MaterialGLSL
import mogot.gl.Shader
import mogot.math.Matrix4fc

class DynamicMaterialGLSL(gl: GL) : MaterialGLSL(gl) {
    private val standart = SimpleMaterial(gl)

    var dynamicShader: Shader? = null
    override val shader: Shader
        get() = dynamicShader ?: standart.shader

    override fun close() {
        standart.close()
        dynamicShader?.close()
    }

}

class ShaderEditViewer : View3D() {
    lateinit var material: DynamicMaterialGLSL

    private var vp = ""
    private var fp = ""
    private var vpOld = ""
    private var fpOld = ""
    private val fpPrefix = """#version 300 es
    
#ifdef GL_ES
precision mediump float;
#endif
"""

    override fun render() {
        if (vp != vpOld || fp != fpOld) {
            vpOld = vp
            fpOld = fp
            try {
                material.dynamicShader?.close()
                material.dynamicShader = Shader(gl, vp, fpPrefix + fp)
            } catch (e: Throwable) {
                println("ERROR")
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
        material = DynamicMaterialGLSL(gl)
        val node = GeomNode2().also {
            it.geom = Geoms.buildCube2(gl, 1f)
            it.material = material
        }
        root!!.addChild(node)

        val l = OmniLight()
        val node1 = GeomNode().apply {
            geom = Geoms.solidSphere(gl, 1f, 30, 30)
            material = SimpleMaterial(gl)
        }
        node1.position.z = -30f
        node1.position.x = 30f
        node1.position.y = 30f
        node1.addChild(l)
        root!!.addChild(node1)
        resetCam()
    }

    override fun dispose() {
        fpOld = ""
        vpOld = ""
        root!!.childs.toTypedArray().forEach {
            if (it !is Camera)
                it.parent = null
        }
    }

    private inner class GeomNode2 : VisualInstance() {
        var geom: Geom3D2? = null
        var material: Material? = null
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
            material?.close()
        }
    }
}