package pw.binom.sceneEditor

import mogot.*
import mogot.math.*
import mogot.rendering.Display
import pw.binom.FloatDataBuffer
import pw.binom.IntDataBuffer

private const val SIZE = 9f

class CenterNode2D(val node: Spatial2D, val view: SceneEditorView) : VisualInstance2D(view.engine) {

    private var geom by ResourceHolder<Geom2D>()
    private var innerMat by ResourceHolder<MInstance>()
    private var outterMat by ResourceHolder<MInstance>()

    override fun render(model: Matrix4fc, modelView: Matrix4fc, projection: Matrix4fc, context: Display.Context) {
        if (!visible)
            return
        if (node.isVisualInstance2D() && !node.visible)
            return
        if (innerMat == null) {
            innerMat = view.default3DMaterial.instance(Vector4f(0f, 0f, 0f, 1f))
        }
        if (outterMat == null) {
            outterMat = view.default3DMaterial.instance(Vector4f(1f, 1f, 1f, 1f))
        }
        if (geom == null) {
            val index = IntDataBuffer.alloc(4)
            (0 until 4).forEach {
                index[it] = it
            }
            val vertex = FloatDataBuffer.alloc(4 * 2)
            vertex[0] = 0f
            vertex[1] = -SIZE * 0.5f

            vertex[2] = 0f
            vertex[3] = SIZE * 0.5f

            vertex[4] = -SIZE * 0.5f
            vertex[5] = 0f

            vertex[6] = SIZE * 0.5f
            vertex[7] = 0f

            val g = Geom2D(engine.gl, index, vertex, null, null)
            g.mode = Geometry.RenderMode.LINES
            index.close()
            vertex.close()
            geom = g
        }
        val mat = engine.mathPool.mat4f.poll()
        //node.parentSpatial2D?.let { mat.set(it.matrix) } ?: mat.identity()
        mat.set(node.matrix)
        mat.setRotationXYZ(0f, 0f, 0f)
        mat.scale(1f / node.scale.x, 1f / node.scale.y, 1f)
        mat.scale(1f / view.editorCamera2D.zoom, 1f / view.editorCamera2D.zoom, 1f)

        engine.gl.gl.glLineWidth(3f)
        outterMat!!.use(mat, modelView, projection, context)
        geom!!.draw()
        outterMat!!.unuse()

        engine.gl.gl.glLineWidth(1f)
        innerMat!!.use(mat, modelView, projection, context)
        geom!!.draw()
        innerMat!!.unuse()
        engine.mathPool.mat4f.push(mat)
    }

    override fun close() {
        geom = null
        super.close()
    }
}