package mogot

import mogot.gl.BufferArray
import mogot.gl.BufferElementArray
import mogot.gl.GL
import mogot.gl.VertexArray
import mogot.math.Vector2fc
import pw.binom.io.Closeable

open class Rect2D(val gl: GL, size: Vector2fc) : Closeable {
    private var _size = size
    val size
        get() = _size

    fun setSize(size: Vector2fc) {
        this._size = size
        vertexBuffer.uploadArray(floatArrayOf(
                0f, 0f,
                size.x, 0f,
                size.x, size.y,
                0f, size.y
        ))
    }

    private val vertexBuffer = BufferArray(gl = gl, static = true, draw = true)
    private val uvBuffer = BufferArray(gl = gl, static = true, draw = true).apply {
        uploadArray(floatArrayOf(
                0f, 0f,
                1f, 0f,
                1f, 1f,

                0f, 1f
        ))
    }
    private val vao = VertexArray(gl)
    private val vertexSize = 12 / 2
    private val indexBuffer = BufferElementArray(gl, static = true, draw = true)

    init {
        setSize(size)
        vao.bind {
            indexBuffer.uploadArray(intArrayOf(0, 1, 2, 2, 3, 0))
            indexBuffer.bind()

            vertexBuffer.bind()
            gl.vertexAttribPointer(0, 2, gl.FLOAT, false, 0, 0)
            gl.enableVertexAttribArray(0)

            uvBuffer.bind()
            gl.vertexAttribPointer(2, 2, gl.FLOAT, false, 0, 0)
            gl.enableVertexAttribArray(2)

        }
    }

    fun draw() {
        vao.bind {
            gl.drawElements(gl.TRIANGLES, 6, gl.UNSIGNED_INT, 0)
        }
    }

    override fun close() {
        vao.close()
        vertexBuffer.close()
        uvBuffer.close()
    }
}