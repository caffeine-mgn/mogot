package pw.binom.sceneEditor

import mogot.*
import mogot.math.*
import mogot.gl.*
import mogot.rendering.Display
import pw.binom.FloatDataBuffer
import pw.binom.IntDataBuffer
import pw.binom.*

class FrustumNode(val engine: Engine, val camera: Camera) : VisualInstance(), MaterialNode by MaterialNodeImpl() {
    private var geom by ResourceHolder<Geom3D2>()

    override fun apply(matrix: Matrix4fc): Matrix4fc {
        camera.localToGlobalMatrix(this._matrix)
        matrix.mul(this._matrix, this._matrix)
        return matrix
    }

    private var near = 0f
    private var far = 0f
    private var fieldOfView = 0f
    private var oldProjection = Matrix4f()

    private fun update() {
        near = camera.near
        far = camera.far
        fieldOfView = camera.fieldOfView
        oldProjection.set(camera.projectionMatrix)
        val vec = Vector3f()
        geom = null
        val vertex = FloatDataBuffer.alloc(24 * 3)
        val builder = vertex.builder()
        var c = 0

        camera.projectionMatrix.frustumCorner(Matrix4fc.CORNER_NXNYNZ, vec)
        builder.push(vec)

        camera.projectionMatrix.frustumCorner(Matrix4fc.CORNER_PXNYNZ, vec)
        builder.push(vec)

        camera.projectionMatrix.frustumCorner(Matrix4fc.CORNER_PXPYNZ, vec)
        builder.push(vec)

        camera.projectionMatrix.frustumCorner(Matrix4fc.CORNER_NXPYNZ, vec)
        builder.push(vec)

        camera.projectionMatrix.frustumCorner(Matrix4fc.CORNER_PXNYPZ, vec)
        builder.push(vec)

        camera.projectionMatrix.frustumCorner(Matrix4fc.CORNER_NXNYPZ, vec)
        builder.push(vec)

        camera.projectionMatrix.frustumCorner(Matrix4fc.CORNER_NXPYPZ, vec)
        builder.push(vec)

        camera.projectionMatrix.frustumCorner(Matrix4fc.CORNER_PXPYPZ, vec)
        builder.push(vec)

//----------------//
        //--top-right edge--//
        camera.projectionMatrix.frustumCorner(Matrix4fc.CORNER_PXPYPZ, vec)
        builder.push(vec)

        camera.projectionMatrix.frustumCorner(Matrix4fc.CORNER_PXPYNZ, vec)
        builder.push(vec)

        //--bottom-right edge--//
        camera.projectionMatrix.frustumCorner(Matrix4fc.CORNER_PXNYPZ, vec)
        builder.push(vec)

        camera.projectionMatrix.frustumCorner(Matrix4fc.CORNER_PXNYNZ, vec)
        builder.push(vec)

        //--bottom-left edge--//
        camera.projectionMatrix.frustumCorner(Matrix4fc.CORNER_NXNYPZ, vec)
        builder.push(vec)

        camera.projectionMatrix.frustumCorner(Matrix4fc.CORNER_NXNYNZ, vec)
        builder.push(vec)

        //--top-left edge--//
        camera.projectionMatrix.frustumCorner(Matrix4fc.CORNER_NXPYPZ, vec)
        builder.push(vec)

        camera.projectionMatrix.frustumCorner(Matrix4fc.CORNER_NXPYNZ, vec)
        builder.push(vec)

        //--screen-right edge--//
        camera.projectionMatrix.frustumCorner(Matrix4fc.CORNER_PXPYNZ, vec)
        builder.push(vec)

        camera.projectionMatrix.frustumCorner(Matrix4fc.CORNER_PXNYNZ, vec)
        builder.push(vec)

        //--screen-left edge--//
        camera.projectionMatrix.frustumCorner(Matrix4fc.CORNER_NXPYNZ, vec)
        builder.push(vec)

        camera.projectionMatrix.frustumCorner(Matrix4fc.CORNER_NXNYNZ, vec)
        builder.push(vec)

        //--projection-right edge--//
        camera.projectionMatrix.frustumCorner(Matrix4fc.CORNER_PXPYPZ, vec)
        builder.push(vec)

        camera.projectionMatrix.frustumCorner(Matrix4fc.CORNER_PXNYPZ, vec)
        builder.push(vec)

        //--left-right edge--//
        camera.projectionMatrix.frustumCorner(Matrix4fc.CORNER_NXPYPZ, vec)
        builder.push(vec)

        camera.projectionMatrix.frustumCorner(Matrix4fc.CORNER_NXNYPZ, vec)
        builder.push(vec)


        val indexes = IntDataBuffer.alloc(vertex.size) { it }
        geom = Geom3D2(
                gl = engine.gl,
                vertex = vertex,
                index = indexes,
                normals = null,
                uvs = null
        )
        vertex.close()
        indexes.close()
        geom!!.mode = Geometry.RenderMode.LINES
        println("Frustim Geom=${geom!!.hashCode()}")
    }

    override fun close() {
        geom = null
        material.dispose()
        super.close()
    }

    override fun render(model: Matrix4fc, projection: Matrix4fc, context: Display.Context) {
        super.render(model, projection, context)
        val mat = material.value ?: return
        if (near != camera.near ||
                far != camera.far ||
                fieldOfView != camera.fieldOfView) {
            geom?.dec()
            geom = null
        }
        if (geom == null)
            update()
        engine.gl.checkError { "Before Material" }
        mat.use(model, projection, context)
        engine.gl.checkError { "After Material" }
        geom!!.draw()
        mat.unuse()
    }
}