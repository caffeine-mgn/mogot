package pw.binom

import mogot.*
import mogot.gl.GL
import mogot.gl.MaterialGLSL
import mogot.gl.Shader

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

    override fun render() {
        if (vp != vpOld || fp != fpOld) {
            vpOld = vp
            fpOld = fp
            try {
                material.dynamicShader?.close()
                material.dynamicShader = mogot.gl.Shader(gl, vp, fp)
                repaint()
                println("OK")
            } catch (e: Throwable) {
                println("ERROR")
                material.dynamicShader = null
                e.printStackTrace()
            }
        }
        super.render()
    }

    fun setShader(vp: String, fp: String) {
        println("Reset shader")
        this.vp = vp
        this.fp = fp
        repaint()
    }

    override fun init() {
        super.init()
        material = DynamicMaterialGLSL(gl)
        val node = GeomNode().apply {
            geom = Geoms.buildCube2(gl, 1f)
            material = material
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
}