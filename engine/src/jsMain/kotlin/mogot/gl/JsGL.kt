package mogot.gl

import mogot.math.Matrix4fc
import mogot.math.get
import org.khronos.webgl.*

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

    actual fun createBuffer(): GLBuffer = JSBuffer(ctx.createBuffer()!!)

    actual fun deleteBuffer(buffer: GLBuffer) {
        buffer as JSBuffer
        ctx.deleteBuffer(buffer.js)
    }

    actual fun bufferData(target: Int, size: Int, data: FloatArray, usage: Int) {
        val b = Float32Array(data.size)
        b.set(data.unsafeCast<Array<Float>>())
//        ctx.bufferData(target, size, usage)
        ctx.bufferData(target, b, usage)
    }

    actual fun clear(mask: Int) {
        ctx.clear(mask)
    }

    actual fun bindBuffer(target: Int, buffer: GLBuffer?) {
        buffer as JSBuffer?
        ctx.bindBuffer(target, buffer?.js)
    }

    actual fun bufferData(target: Int, size: Int, data: IntArray, usage: Int) {
        val b = Int32Array(data.size)
        b.set(data.unsafeCast<Array<Int>>())

//        ctx.bufferData(target, size, usage)
        ctx.bufferData(target, b, usage)
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
}

private inline class JSBuffer(val js: WebGLBuffer) : GLBuffer
private inline class JSVertexArray(val js: WebGLVertexArray) : GLVertexArray
private inline class JSGLProgram(val js: WebGLProgram) : GLProgram
private inline class JSGLShader(val js: WebGLShader) : GLShader
private inline class JSGLUniformLocation(val js: WebGLUniformLocation) : GLUniformLocation
private inline class JGLTexture(val js: WebGLTexture) : GLTexture