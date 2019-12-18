package mogot

import mogot.math.Matrix4fc


open class CSGBox(val engine: Engine) : CSGPrimitive(), MaterialNode by MaterialNodeImpl() {
    var width: Float = 1f
    var height: Float = 1f
    var depth: Float = 1f
    private var geomNode3D2: Geom3D2? = null

    private fun rebuild() {
        geomNode3D2?.close()
        geomNode3D2 = Geoms.buildCube3(width = width, height = height, depth = depth, gl = engine.gl)
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
        val geom = geomNode3D2!!

        geom.draw {
            material.use(model, projection, renderContext)
        }
        material.unuse()
    }
}