package mogot.gl

import pw.binom.IntDataBuffer
import pw.binom.io.Closeable

class BufferElementArray(val gl: GL, val static: Boolean, val draw: Boolean) : Closeable {
    override fun close() {
        gl.deleteBuffer(id)
    }

    val id = run {
        gl.createBuffer()
    }

    fun uploadArray(data: IntDataBuffer, f: (() -> Unit)? = null) {
        bind {
            gl.bufferData(
                    gl.ELEMENT_ARRAY_BUFFER,
                    Int.SIZE_BYTES * data.size,
                    data,
                    when {
                        static && draw -> gl.STATIC_DRAW
                        !static && draw -> gl.DYNAMIC_DRAW
                        static && !draw -> gl.STATIC_READ
                        !static && !draw -> gl.DYNAMIC_READ
                        else -> TODO()
                    }
            )
            f?.invoke()
        }
    }

    fun bind() {
        gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER, id)
    }

    fun <T> bind(f: () -> T): T {
        try {
            bind()
            return f()
        } finally {
            gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER, null)
        }
    }
}

