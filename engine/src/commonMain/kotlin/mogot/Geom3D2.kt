package mogot

import mogot.gl.BufferArray
import mogot.gl.BufferElementArray
import mogot.gl.GL
import mogot.gl.VertexArray
import mogot.math.Vector3f
import pw.binom.io.Closeable

/**
 * VBO хранит вертиксы, нормали и т.п.
 */
class Geom3D2(val gl: GL, val index: IntArray, val vertex: FloatArray, normals: FloatArray?, uvs: FloatArray?) : Closeable {

    enum class RenderMode {
        TRIANGLES,
        LINES
    }

    private var renderMode: Int = gl.TRIANGLES

    var mode
        get() = when (renderMode) {
            gl.LINES -> RenderMode.LINES
            gl.TRIANGLES -> RenderMode.TRIANGLES
            else -> throw RuntimeException()
        }
        set(value) {
            renderMode = when (value) {
                RenderMode.LINES -> gl.LINES
                RenderMode.TRIANGLES -> gl.TRIANGLES
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

//                gl.glVertexAttribPointer(2, 2, GL2.GL_FLOAT, false, 0, 0)
                gl.vertexAttribPointer(2, 2, gl.FLOAT, false, 0, 0)
//                gl.glEnableVertexAttribArray(2)
//                gl.glEnableVertexAttribArray(2)
                gl.enableVertexAttribArray(2)
            }





            normalBuffer?.bind {
                //                gl.glVertexAttribPointer(1, 3, GL2.GL_FLOAT, false, 0, 0)
                gl.vertexAttribPointer(1, 3, gl.FLOAT, false, 0, 0)
                gl.enableVertexAttribArray(1)
            }

//            uvBuffer?.bind {
//                GL45.glVertexAttribPointer(2, 2, GL45.GL_FLOAT, false, 0, 0)
//                GL45.glEnableVertexAttribArray(2)
//            }

        }

//        println("Vertex:")
//        vertex.forEachIndexed { index, fl ->
//            println("$index => $fl")
//        }
//
//        println("\nIndex:")
//        index.forEachIndexed { index, i ->
//            println("$index => $i")
//        }
//        println("Size: $size")
    }

    fun draw(func: (() -> Unit)? = null) {
//        gl.glLineWidth(30f)
        vao.bind {
            func?.invoke()
//            gl.glDrawElements(GL2.GL_TRIANGLES, size, GL2.GL_UNSIGNED_INT, 0)
            gl.drawElements(renderMode, size, gl.UNSIGNED_INT, 0)
//            gl.glDrawElements(GL2.GL_LINE_STRIP, size, GL2.GL_UNSIGNED_INT, 0)
        }

        checkError()
    }

    override fun close() {
        indexBuffer.close()
        vertexBuffer.close()
        normalBuffer?.close()
        uvBuffer?.close()
    }

    fun checkError() {
        gl.getError().also {
            if (it != 0)
                TODO("error=$it")
        }
    }
}