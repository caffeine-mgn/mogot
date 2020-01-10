package pw.binom.sceneEditor

import mogot.MaterialNode
import mogot.MaterialNodeImpl
import mogot.VisualInstance

import mogot.*
import mogot.math.Matrix4fc

class Line(val engine: Engine) : VisualInstance(), MaterialNode by MaterialNodeImpl() {

    private var geom: Geom3D2? = null

    val size = 100f

    private fun update() {
        check(size > 0f)
        geom?.dec()
        val vertex = FloatArray(3 * 2)
        val sizeHalf = (size * 0.5f)

        var c = 0

        vertex[c++] = sizeHalf
        vertex[c++] = 0f
        vertex[c++] = 0f

        vertex[c++] = -sizeHalf
        vertex[c++] = 0f
        vertex[c++] = 0f

        geom = Geom3D2(
                gl = engine.gl,
                vertex = vertex,
                index = IntArray(vertex.size) { it },
                normals = null,
                uvs = null
        )
        geom!!.mode = Geometry.RenderMode.LINES
        geom!!.inc()
    }

    override fun close() {
        geom?.dec()
        material.dispose()
        super.close()
    }

    override fun render(model: Matrix4fc, projection: Matrix4fc, renderContext: RenderContext) {
        super.render(model, projection, renderContext)
        val mat = material ?: return
        if (geom == null)
            update()
        mat.value?.use(model, projection, renderContext)
        geom!!.draw()
        mat.value?.unuse()
    }
}