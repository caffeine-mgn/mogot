package mogot

import mogot.math.Matrix4fc
import mogot.rendering.Display


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

    override fun render(model: Matrix4fc, projection: Matrix4fc, context: Display.Context) {
        if (geomNode3D2 == null)
            rebuild()
        super.render(model, projection, context)
        val material = material
        val geom = geomNode3D2
        material.value?.use(model, projection, context)
        geom!!.draw()
        material.value?.unuse()
    }

    override fun close() {
        geomNode3D2 = null
        material.dispose()
        super.close()
    }
}