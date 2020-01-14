package mogot.gl

import pw.binom.FloatDataBuffer
import pw.binom.io.Closeable

class BufferArray(val gl: GL, val static: Boolean, val draw: Boolean) : Closeable {
    override fun close() {
        gl.deleteBuffer(id)

    }

    private val id = run {
        gl.createBuffer()

    }

    fun uploadArray(data: FloatDataBuffer, f: (() -> Unit)? = null) {
        bind {
            gl.bufferData(gl.ARRAY_BUFFER, data.size * 4, data, when {
                static && draw -> gl.STATIC_DRAW
                !static && draw -> gl.DYNAMIC_DRAW
                static && !draw -> gl.STATIC_READ
                !static && !draw -> gl.DYNAMIC_READ
                else -> TODO()
            })
            f?.invoke()
        }
    }

    fun bind() {
        gl.bindBuffer(gl.ARRAY_BUFFER, id)
    }

    fun <T> bind(f: () -> T): T {
        try {
            bind()
            return f()
        } finally {
            gl.bindBuffer(gl.ARRAY_BUFFER, null)
        }
    }
}