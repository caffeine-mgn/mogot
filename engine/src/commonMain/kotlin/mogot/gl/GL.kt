package mogot.gl

import mogot.math.Matrix4fc

expect class GL {
    fun createBuffer(): GLBuffer
    fun deleteBuffer(buffer: GLBuffer)
    fun bufferData(target: Int, size: Int, data: FloatArray, usage: Int)
    fun bufferData(target: Int, size: Int, data: IntArray, usage: Int)
    fun bindBuffer(target: Int, buffer: GLBuffer?)

    fun createVertexArray(): GLVertexArray
    fun deleteVertexArray(array: GLVertexArray)
    fun bindVertexArray(array: GLVertexArray?)
    fun enableVertexAttribArray(index: Int)
    fun createProgram(): GLProgram
    fun deleteProgram(program: GLProgram)
    fun createShader(type: Int): GLShader
    fun deleteShader(shader: GLShader)
    fun compileShader(shader: GLShader)

    fun shaderSource(shader: GLShader, source: String)
    fun attachShader(program: GLProgram, shader: GLShader)
    fun linkProgram(program: GLProgram)
    fun useProgram(program: GLProgram?)
    fun getUniformLocation(program: GLProgram, name: String): GLUniformLocation?
    fun uniform1f(uniformLocation: GLUniformLocation, value: Float)
    fun uniform1i(uniformLocation: GLUniformLocation, value: Int)
    fun uniform3f(uniformLocation: GLUniformLocation, x: Float, y: Float, z: Float)
    fun uniform3i(uniformLocation: GLUniformLocation, x: Int, y: Int, z: Int)
    fun uniform4f(uniformLocation: GLUniformLocation, x: Float, y: Float, z: Float, w: Float)
    fun uniform4i(uniformLocation: GLUniformLocation, x: Int, y: Int, z: Int, w: Int)
    fun uniform1fv(location: GLUniformLocation, v: FloatArray)
    fun uniformMatrix4v(location: GLUniformLocation, vararg matrix: Matrix4fc)
    fun activeTexture(texture: Int)
    fun createTexture(): GLTexture
    fun deleteTexture(texture: GLTexture)
    fun bindTexture(target: Int, texture: GLTexture?)
    fun getShaderInfoLog(shader: GLShader): String?
    fun getShaderi(shader: GLShader, type: Int): Int
    fun getProgrami(program: GLProgram, type: Int): Int
    fun getProgramInfoLog(program: GLProgram): String?
    fun bindFramebuffer(target: Int, frameBuffer: Int)
    fun genFramebuffers(): Int
    fun texImage2D(target: Int, level: Int, internalformat: Int, width: Int, height: Int, border: Int, format: Int, type: Int, pixels: Long)
    fun texParameteri(target: Int, pname: Int, param: Int)
    fun framebufferTexture2D(target: Int, attachmen: Int, textarget: Int, texture: Int, level: Int)
    fun genRenderbuffers(): Int
    fun bindRenderbuffer(target: Int, renderbuffer: Int)
    fun renderbufferStorage(target: Int, internalformat: Int, width: Int, height: Int)
    fun framebufferRenderbuffer(target:Int, attachment:Int, renderbuffertarget:Int, renderbuffer:Int)
    fun checkFramebufferStatus(target: Int):Int

    fun vertexAttribPointer(index: Int, size: Int, type: Int, normalized: Boolean, stride: Int, offset: Int)
    fun drawElements(mode: Int, count: Int, type: Int, offset: Int)

    fun getError(): Int

    val STATIC_DRAW: Int
    val DYNAMIC_DRAW: Int
    val STATIC_READ: Int
    val DYNAMIC_READ: Int
    val ARRAY_BUFFER: Int
    val ELEMENT_ARRAY_BUFFER: Int
    val FLOAT: Int
    val TRIANGLES: Int
    val UNSIGNED_INT: Int
    val VERTEX_SHADER: Int
    val FRAGMENT_SHADER: Int
    val COMPILE_STATUS: Int
    val LINK_STATUS: Int
    val TEXTURE0: Int
    val TEXTURE_2D: Int
    val TEXTURE_MAX_LEVEL:Int
}

interface GLBuffer
interface GLVertexArray
interface GLProgram
interface GLShader
interface GLUniformLocation
interface GLTexture