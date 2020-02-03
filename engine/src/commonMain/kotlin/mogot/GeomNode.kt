package mogot

import mogot.gl.DepthShader
import mogot.math.Matrix4fc


class GeomNode : VisualInstance(), MaterialNode by MaterialNodeImpl() {
    val geom = ResourceHolder<Geometry>()

    override fun render(model: Matrix4fc, projection: Matrix4fc, renderContext: RenderContext) {
        material.value?.use(model, projection, renderContext)
        geom.value?.draw()
        material.value?.unuse()
    }

    override fun renderToShadowMap(model: Matrix4fc, view:Matrix4fc, projection: Matrix4fc, renderContext: RenderContext, shader: DepthShader) {
        if(shadow) {
            shader.use()
            shader.uniform("projection", projection)
            shader.uniform("view", view)
            shader.uniform("model", model)
            geom.value?.draw()
        }
    }

    override fun free() {
        super.free()
        geom.dispose()
        material.dispose()
    }
}