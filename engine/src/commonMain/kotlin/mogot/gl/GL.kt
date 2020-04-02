@file:JvmName("GLCommonKt")

package mogot.gl

import mogot.math.Matrix4fc
import pw.binom.FloatDataBuffer
import pw.binom.IntDataBuffer
import kotlin.jvm.JvmName

expect class GL {
    fun generateMipmap(target: Int)
    fun clearColor(r: Float, g: Float, b: Float, a: Float)
    fun blendFunc(sfactor: Int, dfactor: Int)
    fun viewport(x: Int, y: Int, w: Int, h: Int)
    fun clear(mask: Int)
    fun createBuffer(): GLBuffer
    fun deleteBuffer(buffer: GLBuffer)
    fun bufferData(target: Int, size: Int, data: FloatDataBuffer, usage: Int)
    fun bufferData(target: Int, size: Int, data: IntDataBuffer, usage: Int)
    fun bindBuffer(target: Int, buffer: GLBuffer?)
    fun drawBuffer(mode:Int)
    fun readBuffer(mode: Int)

    fun createVertexArray(): GLVertexArray
    fun deleteVertexArray(array: GLVertexArray)
    fun bindVertexArray(array: GLVertexArray?)
    fun enableVertexAttribArray(index: Int)
    fun disableVertexAttribArray(index: Int)
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
    fun uniform1b(uniformLocation: GLUniformLocation, value: Boolean)
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
    fun bindFrameBuffer(target: Int, frameBuffer: GLFrameBuffer?)
    fun createFrameBuffer(): GLFrameBuffer
    fun texImage2D(target: Int, level: Int, internalformat: Int, width: Int, height: Int, border: Int, format: Int, type: Int, pixels: Long?)
    fun texImage2DMultisample(target: Int, samples: Int, internalformat: Int, width: Int, height: Int, fixedsamplelocations: Boolean)
    fun texParameteri(target: Int, pname: Int, param: Int)
    fun framebufferTexture2D(target: Int, attachment: Int, textarget: Int, texture: GLTexture, level: Int)
    fun createRenderBuffer(): GLRenderBuffer
    fun bindRenderBuffer(target: Int, renderbuffer: GLRenderBuffer?)
    fun renderbufferStorage(target: Int, internalformat: Int, width: Int, height: Int)
    fun renderbufferStorageMultisample(target: Int, samples: Int, internalformat: Int, width: Int, height: Int)
    fun framebufferRenderbuffer(target: Int, attachment: Int, renderbuffertarget: Int, renderbuffer: GLRenderBuffer)
    fun checkFramebufferStatus(target: Int): Int
    fun getIntegerv(target: Int): Int

    fun vertexAttribPointer(index: Int, size: Int, type: Int, normalized: Boolean, stride: Int, offset: Int)
    fun drawElements(mode: Int, count: Int, type: Int, offset: Int)

    fun getError(): Int
    fun deleteBuffers(texture: GLTexture)
    fun deleteBuffer(buffer: GLRenderBuffer)
    fun deleteBuffer(buffer: GLFrameBuffer)
    fun enable(feature: Int)
    fun disable(feature: Int)
    fun texParameterf(target: Int, pname: Int, param: Float)
    fun glBlitFramebuffer(  srcX0: Int,srcY0: Int,srcX1: Int, srcY1: Int, dstX0: Int, dstY0: Int, dstX1: Int, dstY1: Int, mask: Int, filter: Int)
    fun viewPort(x:Int, y: Int, width:Int, height: Int)
    fun copyTexSubImage2D(target:Int, level:Int, xoffset:Int, yoffset:Int, x:Int, y:Int, width:Int, height:Int)

    val SRC_ALPHA: Int
    val ONE_MINUS_SRC_ALPHA: Int
    val BLEND: Int
    val MAX_TEXTURE_MAX_ANISOTROPY_EXT: Int
    val NEAREST: Int
    val CULL_FACE: Int
    val DEPTH_TEST: Int
    val DEPTH_BUFFER_BIT: Int
    val COLOR_BUFFER_BIT: Int
    val FRAMEBUFFER_COMPLETE: Int
    val DEPTH_STENCIL_ATTACHMENT: Int
    val DEPTH24_STENCIL8: Int
    val RENDERBUFFER: Int
    val COLOR_ATTACHMENT0: Int
    val DEPTH_ATTACHMENT: Int
    val DEPTH_COMPONEN: Int
    val FRAMEBUFFER: Int
    val READ_FRAMEBUFFER: Int
    val DRAW_FRAMEBUFFER: Int
    val TEXTURE_MAG_FILTER: Int
    val LINEAR: Int
    val TEXTURE_MIN_FILTER: Int
    val UNSIGNED_BYTE: Int
    val RGB: Int
    val RGBA: Int
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
    val TEXTURE_2D_MULTISAMPLE: Int
    val TEXTURE_MAX_LEVEL: Int
    val LINES: Int
    val LINE_STRIP: Int
    val LINE_LOOP: Int
    val TRIANGLE_STRIP: Int
    val TRIANGLE_FAN: Int
    val MULTISAMPLE: Int
    val TEXTURE_WRAP_S: Int
    val TEXTURE_WRAP_T: Int
    val CLAMP_TO_EDGE: Int
    val MIRRORED_REPEAT: Int
    val REPEAT: Int
    val NEAREST_MIPMAP_NEAREST: Int
    val LINEAR_MIPMAP_NEAREST: Int
    val NEAREST_MIPMAP_LINEAR: Int
    val LINEAR_MIPMAP_LINEAR: Int
    val MAX_TEXTURE_SIZE: Int
    val MAX_SAMPLES: Int
    val NONE: Int
}

interface GLBuffer
interface GLVertexArray
interface GLProgram
interface GLShader
interface GLUniformLocation
interface GLTexture
interface GLFrameBuffer
interface GLRenderBuffer

inline fun GL.checkError() {
    val err = getError()
    if (err != 0)
        throw IllegalStateException("OpenGL Error #$err")
}

inline fun GL.checkError(message: () -> String) {
    val err = getError()
    if (err != 0)
        throw IllegalStateException("OpenGL Error #$err: ${message()}")
}