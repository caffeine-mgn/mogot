package pw.binom.sceneEditor

import mogot.*
import mogot.math.Matrix4fc

class Grid(engine: Engine) : VisualInstance() {
    /**
     * кол-во квадратов
     */
    private val size = 15

    /**
     * размер одного квадрата
     */
    private val size1 = 1f
    private var geom: Geom3D2
    var material: Material? = null

    init {
        check(size>0)
        check(size1>0f)
        val vertex = FloatArray((size + 1) * 2 * 3 * 2)
        val sizeHalf = (size / 2f) * size1
        val sizeFull = (size) * size1
        var c = 0
        for (x in 0..size) {
            vertex[c++] = x * size1 - sizeHalf
            vertex[c++] = 0f
            vertex[c++] = -sizeHalf

            vertex[c++] = x * size1 - sizeHalf
            vertex[c++] = 0f
            vertex[c++] = sizeHalf
        }

        for (y in 0..size) {
            vertex[c++] = -sizeHalf
            vertex[c++] = 0f
            vertex[c++] = y * size1 - sizeHalf

            vertex[c++] = sizeHalf
            vertex[c++] = 0f
            vertex[c++] = y * size1 - sizeHalf
        }
        geom = Geom3D2(
                gl = engine.gl,
                vertex = vertex,
                index = IntArray(vertex.size) { it },
                normals = null,
                uvs = null
        )
        geom.mode = Geometry.RenderMode.LINES
    }

    override fun render(model: Matrix4fc, projection: Matrix4fc, renderContext: RenderContext) {
        super.render(model, projection, renderContext)
        val mat = material ?: return

        mat.use(model, projection, renderContext)
        geom.draw()
        mat.unuse()
    }
}