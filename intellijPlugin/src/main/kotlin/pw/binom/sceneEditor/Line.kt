package pw.binom.sceneEditor

import mogot.MaterialNode
import mogot.MaterialNodeImpl
import mogot.VisualInstance

import mogot.*
import mogot.math.Matrix4fc
import mogot.math.Vector3f
import mogot.rendering.Display
import pw.binom.FloatDataBuffer
import pw.binom.IntDataBuffer
import pw.binom.alloc
import pw.binom.sceneEditor.editors.Axis

class Line(val engine: Engine) : VisualInstance(), MaterialNode by MaterialNodeImpl() {

    private var geom by ResourceHolder<Geom3D2>()

    val size = 100f
    private var needUpdate = true
    private val axisVec = Vector3f(1f, 0f, 0f)
    private val vertex = FloatDataBuffer.alloc(3 * 2)
    private val index = IntDataBuffer.alloc(vertex.size) { it }
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
        val sizeHalf = (size * 0.5f)

        var c = 0

        vertex[c++] = axisVec.x * sizeHalf
        vertex[c++] = axisVec.y * sizeHalf
        vertex[c++] = axisVec.z * sizeHalf

        vertex[c++] = -axisVec.x * sizeHalf
        vertex[c++] = -axisVec.y * sizeHalf
        vertex[c++] = -axisVec.z * sizeHalf

        if (geom == null)
            geom = Geom3D2(
                    gl = engine.gl,
                    vertex = vertex,
                    index = index,
                    normals = null,
                    uvs = null
            )
        else
            geom!!.vertexBuffer.uploadArray(vertex)

        geom!!.mode = Geometry.RenderMode.LINES
        needUpdate = false
    }

    override fun close() {
        geom = null
        vertex.close()
        index.close()
        material.dispose()
        super.close()
    }

    override fun render(model: Matrix4fc, projection: Matrix4fc, context: Display.Context) {
        super.render(model, projection, context)
        val mat = material
        if (geom == null || needUpdate)
            update()
        mat.value?.use(model, projection, context)
        geom!!.draw()
        mat.value?.unuse()
    }
}