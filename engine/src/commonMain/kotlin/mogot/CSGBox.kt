package mogot

import mogot.math.Matrix4fc


open class CSGBox(val engine: Engine) : CSGPrimitive(), MaterialNode by MaterialNodeImpl() {
    var width: Float = 1f
    var height: Float = 1f
    var depth: Float = 1f
    private var geomNode3D2 = ResourceHolder<Geometry>()

    private fun rebuild() {
        geomNode3D2.value = Geoms.buildCube3(width = width, height = height, depth = depth, gl = engine.gl)
    }

    override fun onStart() {
        super.onStart()
        rebuild()
    }

    override fun render(model: Matrix4fc, projection: Matrix4fc, renderContext: RenderContext) {
        if (geomNode3D2.value == null)
            rebuild()
        super.render(model, projection, renderContext)
        val material = material ?: return
        val geom = geomNode3D2!!
        material.value?.use(model, projection, renderContext)
        geom.value!!.draw()
        material.value?.unuse()
    }

    override fun close() {
        geomNode3D2.dispose()
        material.dispose()
        super.close()
    }
}