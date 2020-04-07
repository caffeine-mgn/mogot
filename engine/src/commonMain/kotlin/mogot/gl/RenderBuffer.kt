package mogot.gl

import pw.binom.io.Closeable

class RenderBuffer(val gl: GL, private var width: Int, private var height: Int, val format: RenderBufferFormat, val msaa: TextureObject.MSAALevels) : Closeable {
    val rbo = gl.createRenderBuffer()

    init {
        bind()
        gl.checkError { "" }
        if (msaa == TextureObject.MSAALevels.Disable)
            gl.renderbufferStorage(gl.RENDERBUFFER, getRenderBufferFormat(gl, format), width, height)
        else {
            gl.renderbufferStorageMultisample(gl.RENDERBUFFER, msaa.level, getRenderBufferFormat(gl, format), width, height)
        }
        gl.checkError { "" }
        unbind()
        gl.checkError { "" }
    }

    fun bind() {
        gl.bindRenderBuffer(gl.RENDERBUFFER, rbo)
    }

    fun unbind() {
        gl.bindRenderBuffer(gl.RENDERBUFFER, null)
    }

    override fun close() {
        gl.deleteBuffer(rbo)
    }

    fun resize(w: Int, h: Int) {
        width = w
        height = h
        bind()
        if (msaa == TextureObject.MSAALevels.Disable)
            gl.renderbufferStorage(gl.RENDERBUFFER, getRenderBufferFormat(gl, format), width, height)
        else {
            gl.renderbufferStorageMultisample(gl.RENDERBUFFER, msaa.level, getRenderBufferFormat(gl, format), width, height)
        }
        gl.checkError { "" }
        unbind()
    }

}