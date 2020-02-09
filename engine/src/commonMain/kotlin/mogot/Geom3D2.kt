package mogot

import mogot.gl.BufferArray
import mogot.gl.BufferElementArray
import mogot.gl.*
import mogot.gl.VertexArray
import mogot.math.Vector3f
import pw.binom.FloatDataBuffer
import pw.binom.IntDataBuffer
import pw.binom.io.Closeable

/**
 * VBO хранит вертиксы, нормали и т.п.
 */
class Geom3D2(val gl: GL, val index: IntDataBuffer, vertex: FloatDataBuffer, normals: FloatDataBuffer?, uvs: FloatDataBuffer?) : Geometry, ResourceImpl() {

    private var renderMode: Int = gl.TRIANGLES

    override var mode
        get() = when (renderMode) {
            gl.LINE_STRIP -> Geometry.RenderMode.LINES
            gl.LINE_STRIP -> Geometry.RenderMode.LINES_STRIP
            gl.TRIANGLES -> Geometry.RenderMode.TRIANGLES
            else -> throw RuntimeException()
        }
        set(value) {
            renderMode = when (value) {
                Geometry.RenderMode.LINES -> gl.LINES
                Geometry.RenderMode.LINES_STRIP -> gl.LINE_STRIP
                Geometry.RenderMode.TRIANGLES -> gl.TRIANGLES
            }
        }

    val indexBuffer = BufferElementArray(gl, static = true, draw = true)
    val vertexBuffer = BufferArray(gl, static = true, draw = true)
    val normalBuffer = if (normals == null) null else BufferArray(gl, static = true, draw = true).also { it.uploadArray(normals) }
    //    val uvBuffer = if (uvs == null) null else BufferArray(static = true, draw = true).also { it.uploadArray(uvs) }
    var uvBuffer: BufferArray? = null
    val vao = VertexArray(gl)

    val size = index.size
    val boxMin = Vector3f()
    val boxMax = Vector3f()
    private var closed = false

    init {

        (0 until vertex.size / 3).forEach {
            val x = vertex[it + 0]
            val y = vertex[it + 1]
            val z = vertex[it + 2]

            if (x < boxMin.x)
                boxMin.x = x

            if (y < boxMin.y)
                boxMin.y = y

            if (z < boxMin.z)
                boxMin.z = z

            if (x > boxMax.x)
                boxMax.x = x

            if (y > boxMax.y)
                boxMax.y = y

            if (z > boxMax.z)
                boxMax.z = z
        }
        vao.bind {
            indexBuffer.uploadArray(index)
            indexBuffer.bind()

            vertexBuffer.uploadArray(vertex)
            vertexBuffer.bind()

            gl.vertexAttribPointer(0, 3, gl.FLOAT, false, 0, 0)
            gl.enableVertexAttribArray(0)

            uvs?.let { uvs ->
                val uvBuffer = BufferArray(gl, static = true, draw = true)
                uvBuffer.uploadArray(uvs)
                uvBuffer.bind()
                this.uvBuffer = uvBuffer
                gl.vertexAttribPointer(2, 2, gl.FLOAT, false, 0, 0)
                gl.enableVertexAttribArray(2)
            }

            normalBuffer?.bind {
                gl.vertexAttribPointer(1, 3, gl.FLOAT, false, 0, 0)
                gl.enableVertexAttribArray(1)
            }
        }
        gl.checkError { "After create geoms" }
        println("Geom created! this=${hashCode()} $vao")
    }

    override fun draw() {
        check(!closed) { "Geom already closed" }
        gl.checkError { "Before render" }
        vao.bind {
            gl.checkError { "this=${hashCode()} Bind error. ${vao}" }
            gl.drawElements(renderMode, size, gl.UNSIGNED_INT, 0)
            gl.checkError()
        }

        gl.checkError()
    }

    override fun dispose() {
        check(!closed) { "Geom already closed" }
        println("Dispose this=${hashCode()} $vao")
        gl.checkError { "Before dispose" }
        vao.bind {
            gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER, null)
            gl.bindBuffer(gl.ARRAY_BUFFER, null)
            gl.disableVertexAttribArray(0)
        }
        vao.close()
        gl.checkError { "1" }
        indexBuffer.close()
        gl.checkError { "1" }
        vertexBuffer.close()
        gl.checkError { "2" }
        normalBuffer?.close()
        gl.checkError { "3" }
        uvBuffer?.close()
        gl.checkError { "4" }

        gl.checkError { "dispose error" }
        super.dispose()
        closed = true
    }
}