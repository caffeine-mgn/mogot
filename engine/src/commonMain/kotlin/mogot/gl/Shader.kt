package mogot.gl

import mogot.math.Matrix4fc
import mogot.math.Vector3fc
import mogot.math.Vector4fc
import pw.binom.io.Closeable

class Shader(val gl: GL, vertex: String, fragment: String) : Closeable {
    val id = gl.createProgram()

    init {
        val f = compileFragmentShader(gl, fragment)
        checkError()
        val v = compileVertexShader(gl, vertex)
        checkError()
        try {
            gl.attachShader(id, v)
            checkError()
            gl.attachShader(id, f)
            checkError()
            gl.linkProgram(id)
            checkError()
        } finally {
            gl.deleteShader(v)
            gl.deleteShader(f)
        }
        val linked = gl.getProgrami(id, gl.LINK_STATUS)
        val programLog = gl.getProgramInfoLog(id)
        checkError()
        if (programLog != null)
            throw RuntimeException("Program Link Error: $programLog")
        if (linked == 0) {
            gl.deleteProgram(id)
            throw RuntimeException("Can't link program")
        }
    }

    override fun close() {
        gl.deleteProgram(id)
    }

    fun use() {
        gl.useProgram(id)
    }

    fun use(f: () -> Unit) {
        try {
            gl.useProgram(id)
            f()
        } finally {
            gl.useProgram(null)
        }
    }

    fun uniform(name: String, x: Float, y: Float, z: Float, w: Float): Boolean {
        val num = gl.getUniformLocation(id, name) ?: return false
        gl.uniform4f(num, x, y, z, w)
        return true
    }

    inline fun uniform(name: String, vector: Vector3fc): Boolean = uniform(name, vector.x(), vector.y(), vector.z())

    fun uniform(name: String, x: Float, y: Float, z: Float): Boolean {
        val num = gl.getUniformLocation(id, name) ?: return false
        gl.uniform3f(num, x, y, z)
        return true
    }

    fun uniform(name: String, vector: Vector4fc) = uniform(name, vector.x(), vector.y(), vector.z(), vector.w())

    fun uniform(name: String, value: Float): Boolean {
        val num = gl.getUniformLocation(id, name) ?: return false
        gl.uniform1f(num, value)
        checkError()
        return true
    }

    fun uniform(name: String, value: Int): Boolean {
        val num = gl.getUniformLocation(id, name) ?: return false
        gl.uniform1i(num, value)
        checkError()
        return true
    }

    fun uniform(name: String, array: FloatArray) {
        val num = gl.getUniformLocation(id, name)
        if (num != null) {
            checkError()
            gl.uniform1fv(num, array)
            checkError()

            uniform("${name}_len", array.size)
        }
    }

    fun uniform(name: String, vararg value: Matrix4fc): Boolean {
        val num = gl.getUniformLocation(id, name) ?: return false
        gl.uniformMatrix4v(num, *value)
        return true
    }

    private fun checkError(name: String? = null) {
        gl.getError().also {
            if (it != 0)
                TODO("error=$it in uniform $name")
        }
    }
}


//fun GL2.glShaderSource(shader: Int, source: String) {
//    val v = IntArray(1)
//    glShaderSource(shader, 1, Array(1) { source }, null, 0)
//}

//fun GL2.glGetProgramInfoLog(shader: Int): String {
//    val l = glGetProgrami(shader, GL2.GL_INFO_LOG_LENGTH)
//    val v = IntArray(1)
//    val b = ByteArray(l + 1)
//    glGetProgramInfoLog(shader, b.size + 1, v, 0, b, 0)
//    return String(b).trim()
//}

//fun GL2.glGetShaderInfoLog(shader: Int): String {
//    val l = glGetShaderi(shader, GL2.GL_INFO_LOG_LENGTH)
//    val v = IntArray(1)
//    val b = ByteArray(l + 1)
//    glGetShaderInfoLog(shader, b.size, v, 0, b, 0)
//    println("Shader Log Length: $l")
//    println("Shader ID: $shader")
//    if (v[0] == 0)
//        return ""
//    return String(b, 0, b.size)
//}

private fun compileFragmentShader(gl: GL, source: String): GLShader {
    val v = gl.createShader(gl.FRAGMENT_SHADER)
    gl.shaderSource(v, source)
    gl.compileShader(v)
    if (gl.getShaderi(v, gl.COMPILE_STATUS) == 0) {
        val message = gl.getShaderInfoLog(v)
        gl.deleteShader(v)
        println("Source:\n")
        source.lines().forEachIndexed { index, s ->
            println("${index + 1}: $s")
        }
        throw RuntimeException("Message: $message")
    }
    return v
}

private fun compileVertexShader(gl: GL, source: String): GLShader {
    val v = gl.createShader(gl.VERTEX_SHADER)
    gl.shaderSource(v, source)
    gl.compileShader(v)

    if (gl.getShaderi(v, gl.COMPILE_STATUS) == 0) {
        val message = gl.getShaderInfoLog(v)
        gl.deleteShader(v)
        println("Vertex Shader:\n$source")
        throw RuntimeException("ID=$v, Message: ${message}")
    }
    return v
}