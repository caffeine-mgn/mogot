package mogot

import mogot.gl.*
import mogot.math.Vector2f
import mogot.math.Vector3f
import pw.binom.FloatDataBuffer
import pw.binom.IntDataBuffer

class Geom2D(val gl: GL, val index: IntDataBuffer, val vertex: FloatDataBuffer, normals: FloatDataBuffer?, uvs: FloatDataBuffer?) : Geometry, ResourceImpl() {

    private var renderMode: Int = gl.TRIANGLES

    override var mode
        get() = when (renderMode) {
            gl.LINES -> Geometry.RenderMode.LINES
            gl.TRIANGLES -> Geometry.RenderMode.TRIANGLES
            else -> throw RuntimeException()
        }
        set(value) {
            renderMode = when (value) {
                Geometry.RenderMode.LINES -> gl.LINES
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
    val boxMin = Vector2f()
    val boxMax = Vector2f()
    private var closed = false

    init {

        (0 until vertex.size / 2).forEach {
            val x = vertex[it + 0]
            val y = vertex[it + 1]

            if (x < boxMin.x)
                boxMin.x = x

            if (y < boxMin.y)
                boxMin.y = y

            if (x > boxMax.x)
                boxMax.x = x

            if (y > boxMax.y)
                boxMax.y = y
        }
        vao.bind {
            indexBuffer.uploadArray(index)
            indexBuffer.bind()

            vertexBuffer.uploadArray(vertex)
            vertexBuffer.bind()

            gl.vertexAttribPointer(0, 2, gl.FLOAT, false, 0, 0)
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
                gl.vertexAttribPointer(1, 2, gl.FLOAT, false, 0, 0)
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