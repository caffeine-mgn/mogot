package mogot

import mogot.math.Matrix4fc


class GeomNode : VisualInstance() {
    var geom: Geom3D2?=null
    var material: Material?=null
    override fun render(model: Matrix4fc, projection: Matrix4fc, renderContext: RenderContext) {
        material?.use(model, projection, renderContext)
        geom?.draw()
        material?.unuse()
    }

    override fun free() {
        super.free()
        geom?.close()
    }
}