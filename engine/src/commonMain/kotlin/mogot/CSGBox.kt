package mogot

import mogot.gl.DepthShader
import mogot.math.Matrix4fc


open class CSGBox(val engine: Engine) : CSGPrimitive(), MaterialNode by MaterialNodeImpl() {
    var width: Float = 1f
    var height: Float = 1f
    var depth: Float = 1f
    private var geomNode3D2 by ResourceHolder<Geometry>()

    private fun rebuild() {
        geomNode3D2 = Geoms.buildCube3(width = width / 2f, height = height / 2f, depth = depth / 2f, gl = engine.gl)
    }

    override fun onStart() {
        super.onStart()
        rebuild()
    }

    override fun render(model: Matrix4fc, projection: Matrix4fc, renderContext: RenderContext) {
        if (geomNode3D2 == null)
            rebuild()
        super.render(model, projection, renderContext)
        val material = material ?: return
        val geom = geomNode3D2
        material.value?.use(model, projection, renderContext)
        geom!!.draw()
        material.value?.unuse()
    }

    override fun renderToShadowMap(model: Matrix4fc, view: Matrix4fc, projection: Matrix4fc, renderContext: RenderContext, shader: DepthShader) {
        super.renderToShadowMap(model, view, projection, renderContext, shader)
        if(shadow) {
            val geom = geomNode3D2
            shader.use()
            shader.uniform("projection", projection)
            shader.uniform("view", view)
            shader.uniform("model", model)
            geom!!.draw()
        }
    }

    override fun close() {
        geomNode3D2 = null
        material.dispose()
        super.close()
    }
}