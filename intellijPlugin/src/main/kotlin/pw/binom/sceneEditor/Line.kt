package pw.binom.sceneEditor

import mogot.MaterialNode
import mogot.MaterialNodeImpl
import mogot.VisualInstance

import mogot.*
import mogot.math.Matrix4fc
import mogot.math.Vector3f
import pw.binom.sceneEditor.editors.Axis

class Line(val engine: Engine) : VisualInstance(), MaterialNode by MaterialNodeImpl() {

    private var geom by ResourceHolder<Geom3D2>()

    val size = 100f
    private var needUpdate = true
    private val axisVec = Vector3f(1f, 0f, 0f)
    var axis = Axis.X
        set(value) {
            if (field == value)
                return
            field = value
            when (value) {
                Axis.X -> axisVec.set(1f, 0f, 0f)
                Axis.Y -> axisVec.set(0f, 1f, 0f)
                Axis.Z -> axisVec.set(0f, 0f, 1f)
            }
            needUpdate = true
        }

    private fun update() {
        check(size > 0f)
        geom = null
        val vertex = FloatArray(3 * 2)
        val sizeHalf = (size * 0.5f)

        var c = 0

        vertex[c++] = axisVec.x * sizeHalf
        vertex[c++] = axisVec.y * sizeHalf
        vertex[c++] = axisVec.z * sizeHalf

        vertex[c++] = -axisVec.x * sizeHalf
        vertex[c++] = -axisVec.y * sizeHalf
        vertex[c++] = -axisVec.z * sizeHalf

        geom = Geom3D2(
                gl = engine.gl,
                vertex = vertex,
                index = IntArray(vertex.size) { it },
                normals = null,
                uvs = null
        )
        geom!!.mode = Geometry.RenderMode.LINES
        needUpdate = false
    }

    override fun close() {
        geom = null
        material.dispose()
        super.close()
    }

    override fun render(model: Matrix4fc, projection: Matrix4fc, renderContext: RenderContext) {
        super.render(model, projection, renderContext)
        val mat = material ?: return
        if (geom == null || needUpdate)
            update()
        mat.value?.use(model, projection, renderContext)
        geom!!.draw()
        mat.value?.unuse()
    }
}