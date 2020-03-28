package mogot

import mogot.math.Matrix4fc
import mogot.rendering.Display


class GeomNode : VisualInstance(), MaterialNode by MaterialNodeImpl() {
    val geom = ResourceHolder<Geometry>()

    override fun render(model: Matrix4fc, projection: Matrix4fc, context: Display.Context) {
        material.value?.use(model, projection, context)
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