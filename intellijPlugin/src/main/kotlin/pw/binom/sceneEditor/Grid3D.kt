package pw.binom.sceneEditor

import mogot.*
import mogot.gl.GL
import mogot.math.Matrix4fc
import mogot.rendering.Display
import pw.binom.FloatDataBuffer
import pw.binom.intDataOf

class Grid3D(val gl: GL) : VisualInstance(), MaterialNode by MaterialNodeImpl() {
    /**
     * кол-во квадратов
     */
    private val size = 15

    /**
     * размер одного квадрата
     */
    private val size1 = 1f
    private var geom by ResourceHolder<Geom3D2>()

    private fun update() {
        check(size > 0)
        check(size1 > 0f)
        val vertex = FloatDataBuffer.alloc((size + 1) * 2 * 3 * 2)
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
        val indexes = intDataOf(*(0 until vertex.size).map { it }.toIntArray())
        geom = Geom3D2(
                gl = gl,
                vertex = vertex,
                index = indexes,
                normals = null,
                uvs = null
        )
        vertex.close()
        indexes.close()
        geom!!.mode = Geometry.RenderMode.LINES
        println("Recreate Grid")
    }

    override fun close() {
        geom = null
        material.dispose()
        super.close()
    }

    override fun render(model: Matrix4fc, modelView: Matrix4fc, projection: Matrix4fc, context: Display.Context) {
        super.render(model, modelView, projection, context)
        val mat = material.value ?: return
        if (geom == null)
            update()
        mat.use(model, modelView, projection, context)
        geom!!.draw()
        mat.unuse()
    }
}