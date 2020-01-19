package mogot

import mogot.gl.BufferArray
import mogot.gl.BufferElementArray
import mogot.gl.GL
import mogot.gl.VertexArray
import mogot.math.Vector2fc
import pw.binom.FloatDataBuffer
import pw.binom.IntDataBuffer
import pw.binom.io.Closeable

open class Rect2D(val gl: GL, size: Vector2fc) : ResourceImpl() {
    private var _size = size
    val size
        get() = _size

    fun setSize(size: Vector2fc) {
        this._size = size
        vertexBuffer.uploadArray(FloatDataBuffer.alloc(8).also{
            it[0] = 0f;it[1] = 0f
            it[2] = size.x;it[3] = 0f
            it[4] = size.x;it[5] = size.y
            it[6] = 0f;it[7] = size.y
        })
    }

    private val vertexBuffer = BufferArray(gl = gl, static = true, draw = true)
    private val uvBuffer = BufferArray(gl = gl, static = true, draw = true).apply {
        uploadArray(FloatDataBuffer.alloc(8).also {
            it[0] = 0f;it[1] = 0f
            it[2] = 1f;it[3] = 0f
            it[4] = 1f;it[5] = 1f
            it[6] = 0f;it[7] = 1f
        }
        )
    }
    private val vao = VertexArray(gl)
    private val vertexSize = 12 / 2
    private val indexBuffer = BufferElementArray(gl, static = true, draw = true)

    init {
        setSize(size)
        vao.bind {
            val indexData = IntDataBuffer.alloc(6).also {
                it[0]=0
                it[1]=1
                it[2]=2
                it[3]=2
                it[4]=3
                it[5]=0
            }
            indexBuffer.uploadArray(indexData)
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

    override fun dispose() {
        vao.close()
        vertexBuffer.close()
        uvBuffer.close()
        super.dispose()
    }
}