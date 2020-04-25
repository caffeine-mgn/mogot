package mogot

import mogot.math.Matrix4fc


open class GeomNode : VisualInstance(), MaterialNode by MaterialNodeImpl() {
    val geom = ResourceHolder<Geometry>()

    override fun render(model: Matrix4fc, projection: Matrix4fc, renderContext: RenderContext) {
        material.value?.use(model, projection, renderContext)
        geom.value?.draw()
        material.value?.unuse()
    }

    override fun close() {
        super.free()
        geom.dispose()
        material.dispose()
        super.close()
    }
}