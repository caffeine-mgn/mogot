package pw.binom.sceneEditor

import mogot.*
import mogot.math.*
import mogot.gl.*

class FrustumNode(val engine: Engine, val camera: Camera) : VisualInstance(), MaterialNode by MaterialNodeImpl() {
    private var geom: Geom3D2? = null

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
        geom?.dec()
        val vertex = FloatArray(24 * 3)
        var c = 0

        camera.projectionMatrix.frustumCorner(Matrix4fc.CORNER_NXNYNZ, vec)
        vertex[c++] = vec.x
        vertex[c++] = vec.y
        vertex[c++] = vec.z

        camera.projectionMatrix.frustumCorner(Matrix4fc.CORNER_PXNYNZ, vec)
        vertex[c++] = vec.x
        vertex[c++] = vec.y
        vertex[c++] = vec.z

        camera.projectionMatrix.frustumCorner(Matrix4fc.CORNER_PXPYNZ, vec)
        vertex[c++] = vec.x
        vertex[c++] = vec.y
        vertex[c++] = vec.z

        camera.projectionMatrix.frustumCorner(Matrix4fc.CORNER_NXPYNZ, vec)
        vertex[c++] = vec.x
        vertex[c++] = vec.y
        vertex[c++] = vec.z

        camera.projectionMatrix.frustumCorner(Matrix4fc.CORNER_PXNYPZ, vec)
        vertex[c++] = vec.x
        vertex[c++] = vec.y
        vertex[c++] = vec.z

        camera.projectionMatrix.frustumCorner(Matrix4fc.CORNER_NXNYPZ, vec)
        vertex[c++] = vec.x
        vertex[c++] = vec.y
        vertex[c++] = vec.z

        camera.projectionMatrix.frustumCorner(Matrix4fc.CORNER_NXPYPZ, vec)
        vertex[c++] = vec.x
        vertex[c++] = vec.y
        vertex[c++] = vec.z

        camera.projectionMatrix.frustumCorner(Matrix4fc.CORNER_PXPYPZ, vec)
        vertex[c++] = vec.x
        vertex[c++] = vec.y
        vertex[c++] = vec.z

//----------------//
        //--top-right edge--//
        camera.projectionMatrix.frustumCorner(Matrix4fc.CORNER_PXPYPZ, vec)
        vertex[c++] = vec.x
        vertex[c++] = vec.y
        vertex[c++] = vec.z

        camera.projectionMatrix.frustumCorner(Matrix4fc.CORNER_PXPYNZ, vec)
        vertex[c++] = vec.x
        vertex[c++] = vec.y
        vertex[c++] = vec.z


        //--bottom-right edge--//
        camera.projectionMatrix.frustumCorner(Matrix4fc.CORNER_PXNYPZ, vec)
        vertex[c++] = vec.x
        vertex[c++] = vec.y
        vertex[c++] = vec.z

        camera.projectionMatrix.frustumCorner(Matrix4fc.CORNER_PXNYNZ, vec)
        vertex[c++] = vec.x
        vertex[c++] = vec.y
        vertex[c++] = vec.z

        //--bottom-left edge--//
        camera.projectionMatrix.frustumCorner(Matrix4fc.CORNER_NXNYPZ, vec)
        vertex[c++] = vec.x
        vertex[c++] = vec.y
        vertex[c++] = vec.z

        camera.projectionMatrix.frustumCorner(Matrix4fc.CORNER_NXNYNZ, vec)
        vertex[c++] = vec.x
        vertex[c++] = vec.y
        vertex[c++] = vec.z

        //--top-left edge--//
        camera.projectionMatrix.frustumCorner(Matrix4fc.CORNER_NXPYPZ, vec)
        vertex[c++] = vec.x
        vertex[c++] = vec.y
        vertex[c++] = vec.z

        camera.projectionMatrix.frustumCorner(Matrix4fc.CORNER_NXPYNZ, vec)
        vertex[c++] = vec.x
        vertex[c++] = vec.y
        vertex[c++] = vec.z


        //--screen-right edge--//
        camera.projectionMatrix.frustumCorner(Matrix4fc.CORNER_PXPYNZ, vec)
        vertex[c++] = vec.x
        vertex[c++] = vec.y
        vertex[c++] = vec.z

        camera.projectionMatrix.frustumCorner(Matrix4fc.CORNER_PXNYNZ, vec)
        vertex[c++] = vec.x
        vertex[c++] = vec.y
        vertex[c++] = vec.z

        //--screen-left edge--//
        camera.projectionMatrix.frustumCorner(Matrix4fc.CORNER_NXPYNZ, vec)
        vertex[c++] = vec.x
        vertex[c++] = vec.y
        vertex[c++] = vec.z

        camera.projectionMatrix.frustumCorner(Matrix4fc.CORNER_NXNYNZ, vec)
        vertex[c++] = vec.x
        vertex[c++] = vec.y
        vertex[c++] = vec.z

        //--projection-right edge--//
        camera.projectionMatrix.frustumCorner(Matrix4fc.CORNER_PXPYPZ, vec)
        vertex[c++] = vec.x
        vertex[c++] = vec.y
        vertex[c++] = vec.z

        camera.projectionMatrix.frustumCorner(Matrix4fc.CORNER_PXNYPZ, vec)
        vertex[c++] = vec.x
        vertex[c++] = vec.y
        vertex[c++] = vec.z

        //--left-right edge--//
        camera.projectionMatrix.frustumCorner(Matrix4fc.CORNER_NXPYPZ, vec)
        vertex[c++] = vec.x
        vertex[c++] = vec.y
        vertex[c++] = vec.z

        camera.projectionMatrix.frustumCorner(Matrix4fc.CORNER_NXNYPZ, vec)
        vertex[c++] = vec.x
        vertex[c++] = vec.y
        vertex[c++] = vec.z




        geom = Geom3D2(
                gl = engine.gl,
                vertex = vertex,
                index = IntArray(vertex.size) { it },
                normals = null,
                uvs = null
        )
        geom!!.mode = Geometry.RenderMode.LINES
        geom!!.inc()
        println("Frustim Geom=${geom!!.hashCode()}")
    }

    override fun close() {
        geom?.dec()
        material.dispose()
        super.close()
    }

    override fun render(model: Matrix4fc, projection: Matrix4fc, renderContext: RenderContext) {
        super.render(model, projection, renderContext)
        val mat = material.value ?: return
        if (near != camera.near ||
                far != camera.far ||
                fieldOfView != camera.fieldOfView) {
            geom?.dec()
            geom = null
        }
        if (geom == null)
            update()
        engine.gl.checkError{"Before Material"}
        mat.use(model, projection, renderContext)
        engine.gl.checkError{"After Material"}
        geom!!.draw()
        mat.unuse()
    }
}