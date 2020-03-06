package mogot.gl

import mogot.math.Matrix4fc
import mogot.math.get
import org.khronos.webgl.*
import pw.binom.ByteDataBuffer
import pw.binom.FloatDataBuffer
import pw.binom.IntDataBuffer

/*
private external class WebGLVAOExtension {
    @JsName("createVertexArrayOES")
    fun createVertexArray(): WebGLVertexArray

    @JsName("deleteVertexArrayOES")
    fun deleteVertexArray(array: WebGLVertexArray)

    @JsName("bindVertexArrayOES")
    fun bindVertexArray(array: WebGLVertexArray?)

    @JsName("isVertexArrayOES")
    fun isVertexArray(array: WebGLVertexArray): Boolean
}
*/
external class WebGLVertexArray

abstract external class WebGL2RenderingContext : WebGLRenderingContext {
    fun createVertexArray(): WebGLVertexArray
    fun deleteVertexArray(array: WebGLVertexArray)
    fun bindVertexArray(array: WebGLVertexArray?)
    fun isVertexArray(array: WebGLVertexArray): Boolean
    fun renderbufferStorageMultisample(target:Int, samples:Int, internalFormat:Int, width:Int, height:Int)
}

actual class GL(val ctx: WebGL2RenderingContext) {
//    private var vaoExt = ctx.getExtension("OES_vertex_array_object").unsafeCast<WebGLVAOExtension>()

    actual val STATIC_DRAW: Int
        get() = WebGLRenderingContext.STATIC_DRAW

    actual val DYNAMIC_DRAW: Int
        get() = WebGLRenderingContext.DYNAMIC_DRAW

    actual val STATIC_READ: Int
        get() = WebGLRenderingContext.STATIC_DRAW

    actual val DYNAMIC_READ: Int
        get() = WebGLRenderingContext.DYNAMIC_DRAW

    actual val ARRAY_BUFFER: Int
        get() = WebGLRenderingContext.ARRAY_BUFFER

    actual val ELEMENT_ARRAY_BUFFER: Int
        get() = WebGLRenderingContext.ELEMENT_ARRAY_BUFFER

    actual val FLOAT: Int
        get() = WebGLRenderingContext.FLOAT

    actual val VERTEX_SHADER: Int
        get() = WebGLRenderingContext.VERTEX_SHADER
    actual val FRAGMENT_SHADER: Int
        get() = WebGLRenderingContext.FRAGMENT_SHADER
    actual val COMPILE_STATUS: Int
        get() = WebGLRenderingContext.COMPILE_STATUS

    actual val TRIANGLES: Int
        get() = WebGLRenderingContext.TRIANGLES
    actual val UNSIGNED_INT: Int
        get() = WebGLRenderingContext.UNSIGNED_INT
    actual val LINK_STATUS: Int
        get() = WebGLRenderingContext.LINK_STATUS

    actual val TEXTURE0: Int
        get() = WebGLRenderingContext.TEXTURE0
    actual val TEXTURE_2D: Int
        get() = WebGLRenderingContext.TEXTURE_2D
    actual val TEXTURE_2D_MULTISAMPLE: Int
        get() = WebGLRenderingContext.TEXTURE_2D
    actual val TEXTURE_MAX_LEVEL: Int
        get() = js("WebGL2RenderingContext.TEXTURE_MAX_LEVEL")
    actual val LINES: Int
        get() = WebGLRenderingContext.LINES
    actual val LINE_STRIP: Int
        get() = WebGLRenderingContext.LINE_STRIP
    actual val LINE_LOOP: Int
        get() = WebGLRenderingContext.LINE_LOOP
    actual val TRIANGLE_STRIP: Int
        get() = WebGLRenderingContext.TRIANGLE_STRIP
    actual val TRIANGLE_FAN: Int
        get() = WebGLRenderingContext.TRIANGLE_FAN

    actual val MAX_TEXTURE_MAX_ANISOTROPY_EXT: Int
        get() = js("WebGL2RenderingContext.MAX_TEXTURE_MAX_ANISOTROPY_EXT")
    actual val NEAREST: Int
        get() = WebGLRenderingContext.NEAREST
    actual val CULL_FACE: Int
        get() = WebGLRenderingContext.CULL_FACE
    actual val DEPTH_TEST: Int
        get() = WebGLRenderingContext.DEPTH_TEST
    actual val DEPTH_BUFFER_BIT: Int
        get() = WebGLRenderingContext.DEPTH_BUFFER_BIT
    actual val COLOR_BUFFER_BIT: Int
        get() = WebGLRenderingContext.COLOR_BUFFER_BIT
    actual val FRAMEBUFFER_COMPLETE: Int
        get() = WebGLRenderingContext.FRAMEBUFFER_COMPLETE
    actual val DEPTH_STENCIL_ATTACHMENT: Int
        get() = WebGLRenderingContext.DEPTH_STENCIL_ATTACHMENT
    actual val DEPTH24_STENCIL8: Int
        get() = js("WebGL2RenderingContext.DEPTH24_STENCIL8")
    actual val RENDERBUFFER: Int
        get() = WebGLRenderingContext.RENDERBUFFER
    actual val COLOR_ATTACHMENT0: Int
        get() = WebGLRenderingContext.COLOR_ATTACHMENT0
    actual val DEPTH_ATTACHMENT: Int
        get() = WebGLRenderingContext.DEPTH_ATTACHMENT
    actual val DEPTH_COMPONEN: Int
        get() = WebGLRenderingContext.DEPTH_COMPONENT
    actual val FRAMEBUFFER: Int
        get() = WebGLRenderingContext.FRAMEBUFFER
    actual val READ_FRAMEBUFFER: Int
        get() = TODO("No in WebGLRenderingContext")
    actual val DRAW_FRAMEBUFFER: Int
        get() = TODO("No in WebGLRenderingContext")
    actual val TEXTURE_MAG_FILTER: Int
        get() = WebGLRenderingContext.TEXTURE_MAG_FILTER
    actual val LINEAR: Int
        get() = WebGLRenderingContext.LINEAR
    actual val TEXTURE_MIN_FILTER: Int
        get() = WebGLRenderingContext.TEXTURE_MIN_FILTER
    actual val UNSIGNED_BYTE: Int
        get() = WebGLRenderingContext.UNSIGNED_BYTE
    actual val RGB: Int
        get() = WebGLRenderingContext.RGB
    actual val RGBA: Int
        get() = WebGLRenderingContext.RGBA
    actual val MULTISAMPLE: Int
        get() = js("WebGL2RenderingContext.MULTISAMPLE")

    actual val TEXTURE_WRAP_S: Int
        get() = WebGLRenderingContext.TEXTURE_WRAP_S
    actual val TEXTURE_WRAP_T: Int
        get() = WebGLRenderingContext.TEXTURE_WRAP_T
    actual val CLAMP_TO_EDGE: Int
        get() = WebGLRenderingContext.CLAMP_TO_EDGE
    actual val MIRRORED_REPEAT: Int
        get() = WebGLRenderingContext.MIRRORED_REPEAT
    actual val REPEAT: Int
        get() = WebGLRenderingContext.REPEAT
    actual val NEAREST_MIPMAP_NEAREST: Int
        get() = WebGLRenderingContext.NEAREST_MIPMAP_NEAREST
    actual val LINEAR_MIPMAP_NEAREST: Int
        get() = WebGLRenderingContext.LINEAR_MIPMAP_NEAREST
    actual val NEAREST_MIPMAP_LINEAR: Int
        get() = WebGLRenderingContext.NEAREST_MIPMAP_LINEAR
    actual val LINEAR_MIPMAP_LINEAR: Int
        get() = WebGLRenderingContext.LINEAR_MIPMAP_LINEAR
    actual val MAX_SAMPLES: Int
        get() = TODO("WebGLRenderingContext.MAX_SAMPLES")
    actual val NONE: Int
        get() = WebGLRenderingContext.NONE

    actual val MAX_TEXTURE_SIZE: Int
        get() = WebGLRenderingContext.MAX_TEXTURE_SIZE

    actual fun createBuffer(): GLBuffer = JSBuffer(ctx.createBuffer()!!)

    actual fun deleteBuffer(buffer: GLBuffer) {
        buffer as JSBuffer
        ctx.deleteBuffer(buffer.js)
    }

    actual fun bufferData(target: Int, size: Int, data: FloatDataBuffer, usage: Int) {
        ctx.bufferData(target, data.buffer, usage)
    }


    actual fun clear(mask: Int) {
        ctx.clear(mask)
    }

    actual fun bindBuffer(target: Int, buffer: GLBuffer?) {
        buffer as JSBuffer?
        ctx.bindBuffer(target, buffer?.js)
    }

    actual fun drawBuffer(mode:Int){
        TODO("drawBuffer(mode)")
    }

    actual fun readBuffer(mode: Int){
        TODO("readBuffer(mode)")
    }

    actual fun bufferData(target: Int, size: Int, data: IntDataBuffer, usage: Int) {
        ctx.bufferData(target, data.buffer, usage)
    }

    actual fun createVertexArray(): GLVertexArray = JSVertexArray(ctx.createVertexArray())

    actual fun deleteVertexArray(array: GLVertexArray) {
        array as JSVertexArray
        ctx.deleteVertexArray(array.js)
    }

    actual fun bindVertexArray(array: GLVertexArray?) {
        array as JSVertexArray?
        ctx.bindVertexArray(array?.js)
    }

    actual fun enableVertexAttribArray(index: Int) {
        ctx.enableVertexAttribArray(index)
    }

    actual fun disableVertexAttribArray(index: Int) {
        ctx.disableVertexAttribArray(index)
    }

    actual fun vertexAttribPointer(index: Int, size: Int, type: Int, normalized: Boolean, stride: Int, offset: Int) {
        ctx.vertexAttribPointer(index, size, type, normalized, stride, offset)
    }

    actual fun drawElements(mode: Int, count: Int, type: Int, offset: Int) {
        ctx.drawElements(mode, count, type, offset)
    }

    actual fun getError(): Int = ctx.getError()
    actual fun createProgram(): GLProgram {
        val p = ctx.createProgram()!!
        println("Created Program $p")
        return JSGLProgram(p)
    }

    actual fun deleteProgram(program: GLProgram) {
        program as JSGLProgram
        ctx.deleteProgram(program.js)
    }

    actual fun createShader(type: Int): GLShader =
            JSGLShader(ctx.createShader(type)!!)

    actual fun deleteShader(shader: GLShader) {
        shader as JSGLShader
        ctx.deleteShader(shader.js)
    }

    actual fun compileShader(shader: GLShader) {
        shader as JSGLShader
        ctx.compileShader(shader.js)
    }

    actual fun shaderSource(shader: GLShader, source: String) {
        shader as JSGLShader
        ctx.shaderSource(shader.js, source)
    }

    actual fun attachShader(program: GLProgram, shader: GLShader) {
        shader as JSGLShader
        program as JSGLProgram
        ctx.attachShader(program.js, shader.js)
    }

    actual fun linkProgram(program: GLProgram) {
        program as JSGLProgram
        ctx.linkProgram(program.js)
    }

    actual fun useProgram(program: GLProgram?) {
        program as JSGLProgram?
        ctx.useProgram(program?.js)
    }

    actual fun getUniformLocation(program: GLProgram, name: String): GLUniformLocation? {
        program as JSGLProgram
        return ctx.getUniformLocation(program.js, name)?.let { JSGLUniformLocation(it) }
    }

    actual fun uniform1f(uniformLocation: GLUniformLocation, value: Float) {
        uniformLocation as JSGLUniformLocation
        ctx.uniform1f(uniformLocation.js, value)
    }

    actual fun uniform1b(uniformLocation: GLUniformLocation, value: Boolean) {
        uniformLocation as JSGLUniformLocation
        ctx.uniform1i(uniformLocation.js, if (value) 1 else 0)
    }

    actual fun uniform1i(uniformLocation: GLUniformLocation, value: Int) {
        uniformLocation as JSGLUniformLocation
        ctx.uniform1i(uniformLocation.js, value)
    }

    actual fun uniform3f(uniformLocation: GLUniformLocation, x: Float, y: Float, z: Float) {
        uniformLocation as JSGLUniformLocation
        ctx.uniform3f(uniformLocation.js, x, y, z)
    }

    actual fun uniform3i(uniformLocation: GLUniformLocation, x: Int, y: Int, z: Int) {
        uniformLocation as JSGLUniformLocation
        ctx.uniform3i(uniformLocation.js, x, y, z)
    }

    actual fun uniform4f(uniformLocation: GLUniformLocation, x: Float, y: Float, z: Float, w: Float) {
        uniformLocation as JSGLUniformLocation
        ctx.uniform4f(uniformLocation.js, x, y, z, w)
    }

    actual fun uniform4i(uniformLocation: GLUniformLocation, x: Int, y: Int, z: Int, w: Int) {
        uniformLocation as JSGLUniformLocation
        ctx.uniform4i(uniformLocation.js, x, y, z, w)
    }

    actual fun uniform1fv(location: GLUniformLocation, v: FloatArray) {
        location as JSGLUniformLocation
        ctx.uniform1fv(location.js, v.unsafeCast<Array<Float>>())
    }

    actual fun getShaderInfoLog(shader: GLShader): String? {
        shader as JSGLShader
        return ctx.getShaderInfoLog(shader.js)
    }

    actual fun getShaderi(shader: GLShader, type: Int): Int {
        shader as JSGLShader
        return ctx.getShaderParameter(shader.js, type).unsafeCast<Int>() + 0
    }

    actual fun getProgrami(program: GLProgram, type: Int): Int {
        program as JSGLProgram
        return ctx.getProgramParameter(program.js, type).unsafeCast<Int>() + 0
    }

    actual fun getProgramInfoLog(program: GLProgram): String? {
        program as JSGLProgram
        return ctx.getProgramInfoLog(program.js)?.trim()?.takeIf { it.isNotEmpty() }
    }

    actual fun uniformMatrix4v(location: GLUniformLocation, vararg matrix: Matrix4fc) {
        location as JSGLUniformLocation
        val data = Float32Array(matrix.size * 16)
        val vv = FloatArray(matrix.size * 16)
        matrix.forEachIndexed { index, matrix4fc ->
            matrix4fc.get(data, index * 16)
            matrix4fc.get(vv, index * 16)
        }
        ctx.uniformMatrix4fv(location.js, false, vv.unsafeCast<Array<Float>>())
    }

    actual fun activeTexture(texture: Int) {
        ctx.activeTexture(texture)
    }

    actual fun createTexture(): GLTexture =
            JGLTexture(ctx.createTexture()!!)

    actual fun deleteTexture(texture: GLTexture) {
        texture as JGLTexture
        ctx.deleteTexture(texture.js)
    }

    actual fun bindTexture(target: Int, texture: GLTexture?) {
        texture as JGLTexture?
        ctx.bindTexture(target, texture?.js)
    }

    actual fun deleteBuffer(buffer: GLRenderBuffer) {
        buffer as JSGLRenderBuffer
        ctx.deleteRenderbuffer(buffer.js)
    }

    actual fun deleteBuffer(buffer: GLFrameBuffer) {
        buffer as JSGLFrameBuffer
        ctx.deleteFramebuffer(buffer.js)
    }

    actual fun bindFrameBuffer(target: Int, frameBuffer: GLFrameBuffer?) {
        frameBuffer as JSGLFrameBuffer?
        ctx.bindFramebuffer(target, frameBuffer?.js)
    }

    actual fun createFrameBuffer(): GLFrameBuffer =
            JSGLFrameBuffer(ctx.createFramebuffer()!!)

    actual fun texImage2D(target: Int, level: Int, internalformat: Int, width: Int, height: Int, border: Int, format: Int, type: Int, pixels: ByteDataBuffer?) {
        TODO()
        //ctx.texImage2D(target,level,internalformat,width,height,border,format,type,pixels)
    }

    actual fun texImage2DMultisample(target: Int, samples: Int, internalformat: Int, width: Int, height: Int, fixedsamplelocations: Boolean){
        TODO()
    }

    actual fun texParameteri(target: Int, pname: Int, param: Int) {
        ctx.texParameteri(target, pname, param)
    }

    actual fun framebufferTexture2D(target: Int, attachment: Int, textarget: Int, texture: GLTexture, level: Int) {
        texture as JGLTexture
        ctx.framebufferTexture2D(target, attachment, textarget, texture.js, level)
    }

    actual fun createRenderBuffer(): GLRenderBuffer =
            JSGLRenderBuffer(ctx.createRenderbuffer()!!)

    actual fun bindRenderBuffer(target: Int, renderbuffer: GLRenderBuffer?) {
        renderbuffer as JSGLRenderBuffer?
        ctx.bindRenderbuffer(target, renderbuffer?.js)
    }

    actual fun renderbufferStorage(target: Int, internalformat: Int, width: Int, height: Int) {
        ctx.renderbufferStorage(target, internalformat, width, height)
    }

    actual fun renderbufferStorageMultisample(target: Int, samples:Int, internalformat: Int, width: Int, height: Int){
        ctx.renderbufferStorageMultisample(target,samples,internalformat,width,height)
    }

    actual fun framebufferRenderbuffer(target: Int, attachment: Int, renderbuffertarget: Int, renderbuffer: GLRenderBuffer) {
        renderbuffer as JSGLRenderBuffer
        ctx.framebufferRenderbuffer(target, attachment, renderbuffertarget, renderbuffer.js)
    }

    actual fun checkFramebufferStatus(target: Int): Int =
            ctx.checkFramebufferStatus(target)

    actual fun getIntegerv(target: Int):Int{
        return ctx.getParameter(target) as Int
    }

    actual fun deleteBuffers(texture: GLTexture) {
        deleteTexture(texture)
    }

    actual fun enable(feature: Int) {
        ctx.enable(feature)
    }

    actual fun disable(feature: Int) {
        ctx.disable(feature)
    }

    actual fun texParameterf(target: Int, pname: Int, param: Float) {
        ctx.texParameterf(target, pname, param)
    }

    actual fun glBlitFramebuffer(  srcX0: Int,srcY0: Int,srcX1: Int, srcY1: Int, dstX0: Int, dstY0: Int, dstX1: Int, dstY1: Int, mask: Int, filter: Int){
        TODO("ctx.blitFramebuffer(srcX0,srcY0,srcX1,srcY1,srcX0,dstY0,dstX1,dstY1,mask,filter)")
    }

    actual fun viewPort(x:Int, y: Int, width:Int, height: Int){
        ctx.viewport(x,y,width,height)
    }
    actual fun copyTexSubImage2D(target:Int, level:Int, xoffset:Int, yoffset:Int, x:Int, y:Int, width:Int, height:Int){
        ctx.copyTexSubImage2D(target, level, xoffset, yoffset, x, y, width, height)
    }
}

private inline class JSBuffer(val js: WebGLBuffer) : GLBuffer
private inline class JSGLRenderBuffer(val js: WebGLRenderbuffer) : GLRenderBuffer
private inline class JSGLFrameBuffer(val js: WebGLFramebuffer) : GLFrameBuffer
private inline class JSVertexArray(val js: WebGLVertexArray) : GLVertexArray
private inline class JSGLProgram(val js: WebGLProgram) : GLProgram
private inline class JSGLShader(val js: WebGLShader) : GLShader
private inline class JSGLUniformLocation(val js: WebGLUniformLocation) : GLUniformLocation
private inline class JGLTexture(val js: WebGLTexture) : GLTexture