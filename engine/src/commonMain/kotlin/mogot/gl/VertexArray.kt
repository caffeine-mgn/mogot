package mogot.gl

import pw.binom.io.Closeable

class VertexArray(val gl: GL) : Closeable {
    private val id = run {
//        val v = IntArray(1)
//        gl.glGenVertexArrays(1, v, 0)
//        v[0]
        gl.createVertexArray()
    }

    fun <T> bind(f: () -> T): T {
        try {
            gl.bindVertexArray(id)
//            gl.glBindVertexArray(id)
            return f()
        } finally {
            gl.bindVertexArray(null)
//            gl.glBindVertexArray(0)
        }
    }

    override fun close() {
        gl.deleteVertexArray(id)
//        gl.glDeleteVertexArrays(id, IntArray(1) { id }, 0)
    }
}