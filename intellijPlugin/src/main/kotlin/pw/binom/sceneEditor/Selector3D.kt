package pw.binom.sceneEditor

import mogot.*
import mogot.math.Matrix4fc

class Selector3D(val engine: Engine, val node: Spatial) : VisualInstance(), MaterialNode {
    val size = Vector3fProperty()
    override val material = ResourceHolder<Material>()
    private val geom = ResourceHolder<Geom3D2>()

    override fun close() {
        geom.dispose()
        super.close()
    }

    private fun rebuildGeom() {

        val vertex = FloatArray(3 * 2 * 3 * 8)
        val lenX = size.x / 100f * 30f
        val lenY = size.y / 100f * 30f
        val lenZ = size.z / 100f * 30f
        var c = 0

        fun draw(x: Float, y: Float, z: Float) {
            vertex[c++] = size.x / 2f * x
            vertex[c++] = size.y / 2f * y
            vertex[c++] = size.z / 2f * z

            vertex[c++] = (size.x / 2f - lenX) * x
            vertex[c++] = size.y / 2f * y
            vertex[c++] = size.z / 2f * z


            vertex[c++] = size.x / 2f * x
            vertex[c++] = size.y / 2f * y
            vertex[c++] = size.z / 2f * z

            vertex[c++] = size.x / 2f * x
            vertex[c++] = (size.y / 2f - lenY) * y
            vertex[c++] = size.z / 2f * z

            vertex[c++] = size.x / 2f * x
            vertex[c++] = size.y / 2f * y
            vertex[c++] = size.z / 2f * z

            vertex[c++] = size.x / 2f * x
            vertex[c++] = size.y / 2f * y
            vertex[c++] = (size.z / 2f - lenZ) * z
        }

        draw(1f, 1f, 1f)
        draw(-1f, 1f, 1f)
        draw(-1f, -1f, 1f)
        draw(-1f, -1f, -1f)

        draw(1f, 1f, -1f)
        draw(1f, -1f, -1f)
        draw(-1f, 1f, -1f)
        draw(1f, -1f, 1f)

        this.geom.value = Geom3D2(
                gl = engine.gl,
                vertex = vertex,
                index = IntArray(vertex.size) { it },
                normals = null,
                uvs = null
        )
        geom.value!!.mode = Geometry.RenderMode.LINES
    }

    override fun apply(matrix: Matrix4fc): Matrix4fc = node.matrix

    override fun render(model: Matrix4fc, projection: Matrix4fc, renderContext: RenderContext) {
        val mat = material ?: return
        if (geom.value == null || size.resetChangeFlag()) {
            rebuildGeom()
        }
        mat.value?.use(model, projection, renderContext)
        geom.value?.draw()
        mat.value?.unuse()
    }
}