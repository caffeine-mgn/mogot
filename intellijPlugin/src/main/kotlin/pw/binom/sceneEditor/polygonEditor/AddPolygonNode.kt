package pw.binom.sceneEditor.polygonEditor

import mogot.*
import mogot.math.Matrix4fc
import mogot.math.Vector4f
import mogot.rendering.Display
import pw.binom.FloatDataBuffer
import pw.binom.IntDataBuffer
import pw.binom.sceneEditor.MInstance
import pw.binom.sceneEditor.SceneEditorView

class AddPolygonNode(val view: SceneEditorView) : VisualInstance2D(view.engine) {
    private var addGeom by ResourceHolder<Geom2D>()
    private var innerMat by ResourceHolder<MInstance>()
    private var outterMat by ResourceHolder<MInstance>()


    fun render(x: Float, y: Float, model: Matrix4fc, modelView: Matrix4fc, projection: Matrix4fc, context: Display.Context) {
        val pos = engine.mathPool.vec3f.poll()
        _matrix.identity()
        model.getTranslation(pos)
        _matrix.scale(1f / view.editorCamera2D.zoom, 1f / view.editorCamera2D.zoom, 1f)
        _matrix.translate(pos)
        _matrix.translate(-x, -y, 0f)
        render(matrix, modelView, projection, context)
    }

    override fun close() {
        addGeom = null
        innerMat = null
        outterMat = null
        super.close()
    }

    override fun render(model: Matrix4fc, modelView: Matrix4fc, projection: Matrix4fc, context: Display.Context) {
        if (innerMat == null) {
            innerMat = view.default3DMaterial.instance(Vector4f.fromColor(255, 0, 255, 0))
        }
        if (outterMat == null) {
            outterMat = view.default3DMaterial.instance(Vector4f.fromColor(255, 0, 0, 0))
        }
        if (addGeom == null) {
            val SIZE = 9f
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
            addGeom = g
        }


        engine.gl.gl.glLineWidth(3f)
        outterMat!!.use(model, modelView, projection, context)
        addGeom!!.draw()
        outterMat!!.unuse()

        engine.gl.gl.glLineWidth(1f)
        innerMat!!.use(model, modelView, projection, context)
        addGeom!!.draw()
        innerMat!!.unuse()
    }
}