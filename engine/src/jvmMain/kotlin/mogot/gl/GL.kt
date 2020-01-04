package mogot.gl

import com.jogamp.opengl.GL2
import com.jogamp.opengl.GL3
import mogot.math.Matrix4fc
import mogot.math.get
import java.nio.Buffer
import java.nio.FloatBuffer
import java.nio.IntBuffer

actual class GL(val gl: GL2) {
    actual fun createBuffer(): GLBuffer {
        val v = IntArray(1)
        gl.glGenBuffers(1, v, 0)
        return JBuffer(v[0])
    }

    actual fun deleteBuffer(buffer: GLBuffer) {
        buffer as JBuffer
        val v = IntArray(1) { buffer.id }
        gl.glDeleteBuffers(1, v, 0)
    }

    actual fun bufferData(target: Int, size: Int, data: FloatArray, usage: Int) {
        val buffer = FloatBuffer.allocate(data.size)
        buffer.put(data)
        buffer.flip2()
        gl.glBufferData(
                target,
                size.toLong(),
                buffer,
                usage
        )
    }

    actual fun bufferData(target: Int, size: Int, data: IntArray, usage: Int) {
        val buffer = IntBuffer.allocate(data.size)
        buffer.put(data)
        buffer.flip2()
        gl.glBufferData(
                target,
                size.toLong(),
                buffer,
                usage
        )
    }

    actual fun bindBuffer(target: Int, buffer: GLBuffer?) {
        buffer as JBuffer?
        gl.glBindBuffer(target, buffer?.id ?: 0)
    }

    actual val STATIC_DRAW: Int
        get() = GL2.GL_STATIC_DRAW
    actual val DYNAMIC_DRAW: Int
        get() = GL2.GL_DYNAMIC_DRAW
    actual val STATIC_READ: Int
        get() = GL2.GL_STATIC_READ
    actual val DYNAMIC_READ: Int
        get() = GL2.GL_DYNAMIC_READ
    actual val ARRAY_BUFFER: Int
        get() = GL2.GL_ARRAY_BUFFER
    actual val ELEMENT_ARRAY_BUFFER: Int
        get() = GL2.GL_ELEMENT_ARRAY_BUFFER
    actual val TEXTURE_2D: Int
        get() = GL2.GL_TEXTURE_2D
    actual val LINES: Int
        get() = GL2.GL_LINES
    actual val LINE_STRIP: Int
        get() = GL2.GL_LINE_STRIP
    actual val LINE_LOOP: Int
        get() = GL2.GL_LINE_LOOP
    actual val TRIANGLE_STRIP: Int
        get() = GL2.GL_TRIANGLE_STRIP
    actual val TRIANGLE_FAN: Int
        get() = GL2.GL_TRIANGLE_FAN

    actual fun createVertexArray(): GLVertexArray {
        val v = IntArray(1)
        gl.glGenVertexArrays(1, v, 0)
        return JVertexArray(v[0])
    }

    actual fun deleteVertexArray(array: GLVertexArray) {
        array as JVertexArray
        gl.glDeleteVertexArrays(1, IntArray(1) { array.id }, 0)
    }

    actual fun bindVertexArray(array: GLVertexArray?) {
        array as JVertexArray?
        gl.glBindVertexArray(array?.id ?: 0)
    }

    actual val FLOAT: Int
        get() = GL2.GL_FLOAT

    actual fun enableVertexAttribArray(index: Int) {
        gl.glEnableVertexAttribArray(index)
    }

    actual fun disableVertexAttribArray(index: Int) {
        gl.glDisableVertexAttribArray(index)
    }

    actual fun vertexAttribPointer(index: Int, size: Int, type: Int, normalized: Boolean, stride: Int, offset: Int) {
        gl.glVertexAttribPointer(index, size, type, normalized, stride, offset.toLong())
    }

    actual fun drawElements(mode: Int, count: Int, type: Int, offset: Int) {
        gl.glDrawElements(mode, count, type, offset.toLong())
    }

    actual val TRIANGLES: Int
        get() = GL2.GL_TRIANGLES
    actual val UNSIGNED_INT: Int
        get() = GL2.GL_UNSIGNED_INT

    actual fun getError(): Int = gl.glGetError()
    actual fun createProgram(): GLProgram = JGLProgram(gl.glCreateProgram())

    actual val VERTEX_SHADER: Int
        get() = GL2.GL_VERTEX_SHADER
    actual val FRAGMENT_SHADER: Int
        get() = GL2.GL_FRAGMENT_SHADER
    actual val COMPILE_STATUS: Int
        get() = GL2.GL_COMPILE_STATUS

    actual val TEXTURE_MAX_LEVEL: Int
        get() = GL2.GL_TEXTURE_MAX_LEVEL

    actual val MULTISAMPLE: Int
        get() = GL2.GL_MULTISAMPLE

    actual fun deleteProgram(program: GLProgram) {
        program as JGLProgram
        gl.glDeleteProgram(program.id)
    }

    actual val LINK_STATUS: Int
        get() = GL2.GL_LINK_STATUS

    actual fun createShader(type: Int): GLShader =
            JGLShader(gl.glCreateShader(type))

    actual fun deleteShader(shader: GLShader) {
        shader as JGLShader
        gl.glDeleteShader(shader.id)
    }

    actual fun compileShader(shader: GLShader) {
        shader as JGLShader
        gl.glCompileShader(shader.id)
    }

    actual fun shaderSource(shader: GLShader, source: String) {
        shader as JGLShader
        gl.glShaderSource(shader.id, 1, Array(1) { source }, null, 0)
    }

    actual fun attachShader(program: GLProgram, shader: GLShader) {
        program as JGLProgram
        shader as JGLShader
        gl.glAttachShader(program.id, shader.id)
    }

    actual fun linkProgram(program: GLProgram) {
        program as JGLProgram
        gl.glLinkProgram(program.id)
    }

    actual fun useProgram(program: GLProgram?) {
        program as JGLProgram?
        gl.glUseProgram(program?.id ?: 0)
    }

    actual fun getUniformLocation(program: GLProgram, name: String): GLUniformLocation? {
        program as JGLProgram
        return gl.glGetUniformLocation(program.id, name)?.takeIf { it >= 0 }?.let { JGLUniformLocation(it) }
    }

    actual fun uniform1f(uniformLocation: GLUniformLocation, value: Float) {
        uniformLocation as JGLUniformLocation
        gl.glUniform1f(uniformLocation.id, value)
    }

    actual fun uniform1i(uniformLocation: GLUniformLocation, value: Int) {
        uniformLocation as JGLUniformLocation
        gl.glUniform1i(uniformLocation.id, value)
    }

    actual fun uniform3f(uniformLocation: GLUniformLocation, x: Float, y: Float, z: Float) {
        uniformLocation as JGLUniformLocation
        gl.glUniform3f(uniformLocation.id, x, y, z)
    }

    actual fun uniform3i(uniformLocation: GLUniformLocation, x: Int, y: Int, z: Int) {
        uniformLocation as JGLUniformLocation
        gl.glUniform3i(uniformLocation.id, x, y, z)
    }

    actual fun uniform4f(uniformLocation: GLUniformLocation, x: Float, y: Float, z: Float, w: Float) {
        uniformLocation as JGLUniformLocation
        gl.glUniform4f(uniformLocation.id, x, y, z, w)
    }

    actual fun uniform4i(uniformLocation: GLUniformLocation, x: Int, y: Int, z: Int, w: Int) {
        uniformLocation as JGLUniformLocation
        gl.glUniform4i(uniformLocation.id, x, y, z, w)
    }

    actual fun uniform1fv(location: GLUniformLocation, v: FloatArray) {
        location as JGLUniformLocation
        gl.glUniform1fv(location.id, v.size, v, 0)
    }

    actual fun getShaderInfoLog(shader: GLShader): String? {
        shader as JGLShader
        val l = gl.glGetShaderi(shader.id, GL2.GL_INFO_LOG_LENGTH)
        if (l == 0)
            return null
        val v = IntArray(1)
        val b = ByteArray(l + 1)
        gl.glGetShaderInfoLog(shader.id, b.size, v, 0, b, 0)
        if (v[0] == 0)
            return null
        return String(b, 0, b.size)
    }

    actual fun getShaderi(shader: GLShader, type: Int): Int {
        shader as JGLShader
        return gl.glGetShaderi(shader.id, type)
    }

    actual fun getProgrami(program: GLProgram, type: Int): Int {
        program as JGLProgram
        return gl.glGetProgrami(program.id, type)
    }

    actual fun getProgramInfoLog(program: GLProgram): String? {
        program as JGLProgram
        val l = getProgrami(program, GL2.GL_INFO_LOG_LENGTH)
        if (l == 0)
            return null
        val v = IntArray(1)
        val b = ByteArray(l + 1)
        if (v[0] == 0)
            return null
        gl.glGetProgramInfoLog(program.id, b.size + 1, v, 0, b, 0)
        return String(b).trim()
    }

    actual fun uniformMatrix4v(location: GLUniformLocation, vararg matrix: Matrix4fc) {
        location as JGLUniformLocation
        val data = FloatArray(matrix.size * 16)
        matrix.forEachIndexed { index, matrix4fc ->
            matrix4fc.get(data, index * 16)
        }
        gl.glUniformMatrix4fv(location.id, 1, false, data, 0)
    }

    actual fun activeTexture(texture: Int) {
        gl.glActiveTexture(texture)
    }

    actual fun bindTexture(target: Int, texture: GLTexture?) {
        texture as JGLTexture?
        gl.glBindTexture(target, texture?.id ?: 0)
    }

    actual val TEXTURE0: Int
        get() = GL2.GL_TEXTURE0

    actual fun createTexture(): GLTexture {
        val b = IntBuffer.allocate(1)
        gl.glGenTextures(1, b)
        return JGLTexture(b[0])
    }

    actual fun deleteTexture(texture: GLTexture) {
        texture as JGLTexture
        val b = IntBuffer.allocate(1)
        b.put(0, texture.id)
        //b.flip()
        gl.glDeleteTextures(1, b)
    }

    actual fun bindFrameBuffer(target: Int, frameBuffer: GLFrameBuffer?) {
        frameBuffer as JGLFrameBuffer?
        gl.glBindFramebuffer(target, frameBuffer?.id ?: 0)
    }

    actual fun createFrameBuffer(): GLFrameBuffer {
        val fbo = IntBuffer.allocate(1)
        gl.glGenFramebuffers(1, fbo)
        return JGLFrameBuffer(fbo[0])
    }

    actual fun texImage2D(target: Int, level: Int, internalformat: Int, width: Int, height: Int, border: Int, format: Int, type: Int, pixels: Long?) {
        if (pixels != null) gl.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels!!)
        else gl.glTexImage2D(target, level, internalformat, width, height, border, format, type, null)
    }

    actual fun texParameteri(target: Int, pname: Int, param: Int) {
        gl.glTexParameteri(target, pname, param)
    }

    actual fun framebufferTexture2D(target: Int, attachment: Int, textarget: Int, texture: GLTexture, level: Int) {
        gl.glFramebufferTexture2D(target, attachment, textarget, (texture as JGLTexture).id, level)
    }

    actual fun createRenderBuffer(): GLRenderBuffer {
        val rbo = IntBuffer.allocate(1)
        gl.glGenRenderbuffers(1, rbo)
        return JGLRenderBuffer(rbo[0])
    }

    actual fun bindRenderBuffer(target: Int, renderbuffer: GLRenderBuffer?) {
        renderbuffer as JGLRenderBuffer?
        gl.glBindRenderbuffer(target, renderbuffer?.id ?: 0)
    }

    actual fun renderbufferStorage(target: Int, internalformat: Int, width: Int, height: Int) {
        gl.glRenderbufferStorage(target, internalformat, width, height)
    }

    actual fun framebufferRenderbuffer(target: Int, attachment: Int, renderbuffertarget: Int, renderbuffer: GLRenderBuffer) {
        renderbuffer as JGLRenderBuffer
        gl.glFramebufferRenderbuffer(target, attachment, renderbuffertarget, renderbuffer.id)
    }

    actual fun checkFramebufferStatus(target: Int): Int {
        return gl.glCheckFramebufferStatus(target)
    }

    actual fun clear(mask: Int) {
        gl.glClear(mask)
    }

    actual val COLOR_ATTACHMENT0: Int
        get() = GL2.GL_COLOR_ATTACHMENT0
    actual val FRAMEBUFFER: Int
        get() = GL2.GL_FRAMEBUFFER
    actual val TEXTURE_MAG_FILTER: Int
        get() = GL2.GL_TEXTURE_MAG_FILTER
    actual val LINEAR: Int
        get() = GL2.GL_LINEAR
    actual val TEXTURE_MIN_FILTER: Int
        get() = GL2.GL_TEXTURE_MIN_FILTER
    actual val UNSIGNED_BYTE: Int
        get() = GL2.GL_UNSIGNED_BYTE
    actual val RGB: Int
        get() = GL2.GL_RGB

    actual fun deleteBuffers(texture: GLTexture) {
        val buffer = IntBuffer.allocate(1)
        buffer.put((texture as JGLTexture).id)
        buffer.flip()
        gl.glDeleteBuffers(1, buffer)
    }

    actual fun deleteBuffer(buffer: GLRenderBuffer) {
        buffer as JGLRenderBuffer
        deleteBuffers(buffer.id)
    }

    actual fun deleteBuffer(buffer: GLFrameBuffer) {
        buffer as JGLFrameBuffer
        deleteBuffers(buffer.id)
    }

    private fun deleteBuffers(buffer: Int) {
        val buffer_ = IntBuffer.allocate(1)
        buffer_.put(buffer)
        buffer_.flip()
        gl.glDeleteBuffers(1, buffer_)
    }

    actual val FRAMEBUFFER_COMPLETE: Int
        get() = GL3.GL_FRAMEBUFFER_COMPLETE
    actual val DEPTH_STENCIL_ATTACHMENT: Int
        get() = GL2.GL_DEPTH_STENCIL_ATTACHMENT
    actual val DEPTH24_STENCIL8: Int
        get() = GL2.GL_DEPTH24_STENCIL8
    actual val RENDERBUFFER: Int
        get() = GL2.GL_RENDERBUFFER

    actual fun enable(feature: Int) {
        gl.glEnable(feature)
    }

    actual fun disable(feature: Int) {
        gl.glDisable(feature)
    }

    actual fun texParameterf(target: Int, pname: Int, param: Float) {
        gl.glTexParameterf(target, pname, param)
    }

    actual val CULL_FACE: Int
        get() = GL2.GL_CULL_FACE
    actual val DEPTH_TEST: Int
        get() = GL2.GL_DEPTH_TEST
    actual val DEPTH_BUFFER_BIT: Int
        get() = GL2.GL_DEPTH_BUFFER_BIT
    actual val COLOR_BUFFER_BIT: Int
        get() = GL2.GL_COLOR_BUFFER_BIT
    actual val NEAREST: Int
        get() = GL2.GL_NEAREST
    actual val MAX_TEXTURE_MAX_ANISOTROPY_EXT: Int
        get() = GL2.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT
}

private fun GL2.glGetShaderi(shader: Int, pname: Int): Int {
    val v = IntArray(1)
    glGetShaderiv(shader, pname, v, 0)
    return v[0]
}


private fun GL2.glGetProgrami(shader: Int, pname: Int): Int {
    val v = IntArray(1)
    glGetProgramiv(shader, pname, v, 0)
    return v[0]
}

fun Buffer.flip2() {
    limit(position())
    position(0)
}

private inline class JBuffer(val id: Int) : GLBuffer
private inline class JVertexArray(val id: Int) : GLVertexArray
private inline class JGLProgram(val id: Int) : GLProgram
private inline class JGLShader(val id: Int) : GLShader
private inline class JGLUniformLocation(val id: Int) : GLUniformLocation
internal inline class JGLTexture(val id: Int) : GLTexture
internal inline class JGLRenderBuffer(val id: Int) : GLRenderBuffer
internal inline class JGLFrameBuffer(val id: Int) : GLFrameBuffer