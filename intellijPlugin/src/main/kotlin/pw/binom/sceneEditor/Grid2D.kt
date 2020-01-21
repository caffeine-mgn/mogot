package pw.binom.sceneEditor

import mogot.*
import mogot.math.Matrix4fc
import mogot.math.Vector4f
import mogot.math.setTranslation
import pw.binom.FloatDataBuffer
import pw.binom.intDataOf
import kotlin.math.floor

class Grid2D(val view: SceneEditorView) : VisualInstance2D(view.engine) {
    private val camera2D: Camera2D
        get() = view.editorCamera2D
    private var vertical by ResourceHolder<Geom2D>()
    private var horizontal by ResourceHolder<Geom2D>()
    private var height = 0
    private var width = 0
    private var verticalData = FloatDataBuffer.alloc(2 * 2)
    private var horizontalData = FloatDataBuffer.alloc(2 * 2)
    private var bgColor by ResourceHolder(view.default3DMaterial.instance(Vector4f(1f, 1f, 1f, 0.1f)))
    private var xAxisMaterial by ResourceHolder(view.default3DMaterial.instance(Vector4f(1f, 0f, 0f, 0.5f)))
    private var yAxisMaterial by ResourceHolder(view.default3DMaterial.instance(Vector4f(0f, 1f, 0f, 0.5f)))

    private fun checkGeoms() {
        if (vertical == null) {
            val index = intDataOf(0, 1)
            vertical = Geom2D(engine.gl, index, verticalData, null, null)
            vertical!!.mode = Geometry.RenderMode.LINES
            index.close()
        }

        if (horizontal == null) {
            val index = intDataOf(0, 1)
            horizontal = Geom2D(engine.gl, index, horizontalData, null, null)
            horizontal!!.mode = Geometry.RenderMode.LINES
            index.close()
        }

        if (width != camera2D.width) {
            width = camera2D.width
            horizontalData[0] = -width * 0.5f; horizontalData[1] = 0f
            horizontalData[2] = width * 0.5f; horizontalData[3] = 0f
            horizontal!!.vertexBuffer.uploadArray(horizontalData)
        }

        if (height != camera2D.height) {
            height = camera2D.height
            verticalData[0] = 0f; verticalData[1] = -height * 0.5f
            verticalData[2] = 0f; verticalData[3] = height * 0.5f;
            vertical!!.vertexBuffer.uploadArray(verticalData)
        }
    }

    private fun toLocalX(x: Float) = (-camera2D.position.x + x) * camera2D.zoom
    private fun toGlobalX(x: Float) = (x) / camera2D.zoom + camera2D.position.x

    private fun toLocalY(y: Float) = (-camera2D.position.y + y) * camera2D.zoom
    private fun toGlobalY(y: Float) = (y) / camera2D.zoom + camera2D.position.y

    override fun close() {
        xAxisMaterial = null
        yAxisMaterial = null
        bgColor = null
        horizontal = null
        vertical = null
        horizontalData.close()
        verticalData.close()
        super.close()
    }

    override fun render(model: Matrix4fc, projection: Matrix4fc, renderContext: RenderContext) {
        checkGeoms()
        val centerX = -camera2D.position.x * camera2D.zoom
        val centerY = -camera2D.position.y * camera2D.zoom
        val mat = engine.mathPool.mat4f.poll()

        run {
            val left = toGlobalX(-width * 0.5f)
            val right = toGlobalX(width * 0.5f)

            val H = 50f
            var x = floor(left / H) * H
            while (x < right) {
                val xx = toLocalX(x)
                mat.setTranslation(xx, 0f, 0f)
                bgColor!!.use(mat, projection, renderContext)
                vertical!!.draw()

                x += H
            }
        }

        run {
            val top = toGlobalY(-height * 0.5f)
            val bottom = toGlobalY(height * 0.5f)

            val H = 50f
            var y = floor(top / H) * H
            while (y < bottom) {
                val xx = toLocalY(y)
                mat.setTranslation(0f, xx, 0f)
                bgColor!!.use(mat, projection, renderContext)
                horizontal!!.draw()
                y += H
            }
        }
        bgColor!!.unuse()

        mat.setTranslation(0f, centerY, 0f)
        xAxisMaterial!!.use(mat, projection, renderContext)
        horizontal!!.draw()
        xAxisMaterial!!.unuse()

        mat.setTranslation(centerX, 0f, 0f)
        yAxisMaterial!!.use(mat, projection, renderContext)
        vertical!!.draw()
        yAxisMaterial!!.unuse()

        engine.mathPool.mat4f.push(mat)
    }
}