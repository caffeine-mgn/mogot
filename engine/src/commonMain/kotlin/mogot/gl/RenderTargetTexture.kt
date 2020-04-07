package mogot.gl

import pw.binom.io.Closeable

class RenderTargetTexture(val gl: GL, val width: Int, val height: Int, val msaa: TextureObject.MSAALevels = TextureObject.MSAALevels.Disable) : Closeable {
    private var isClosed = false

    init {
        val max = gl.getIntegerv(gl.MAX_TEXTURE_SIZE)
        val maxSamples = gl.getIntegerv(gl.MAX_SAMPLES)
        if ((max < width) || (max < height)) {
            throw IllegalArgumentException("Width and Height must be less or Eq to hardware dependent value = $max")
        }
        if (maxSamples < msaa.level)
            throw IllegalArgumentException("MSSA level must be less or Eq to hardware dependent value = $max")
    }

    val msaaFrameBuffer: FrameBuffer? = if (msaa != TextureObject.MSAALevels.Disable)
        FrameBuffer(gl,
                TextureObject(gl, width, height, TextureObject.MinFilterParameter.Nearest, TextureObject.MagFilterParameter.Nearest, TextureObject.TextureWrap.ClampToEdge, TextureObject.TextureWrap.ClampToEdge, msaa),
                RenderBuffer(gl, width, height, RenderBufferFormat.DEPTH24_STENCIL8, msaa)) else null
    val frameBuffer = FrameBuffer(gl,
            TextureObject(gl, width, height, TextureObject.MinFilterParameter.Nearest, TextureObject.MagFilterParameter.Nearest, TextureObject.TextureWrap.ClampToEdge, TextureObject.TextureWrap.ClampToEdge, TextureObject.MSAALevels.Disable, TextureObject.Format.RGBA),
            if (msaa != TextureObject.MSAALevels.Disable) null else RenderBuffer(gl, width, height, RenderBufferFormat.DEPTH24_STENCIL8, TextureObject.MSAALevels.Disable))

    fun begin() {
        if (isClosed)
            throw IllegalStateException("Object is closed")
        if (msaa != TextureObject.MSAALevels.Disable) {
            msaaFrameBuffer?.let {
                it.enable()
                gl.checkError { "" }
                it.bind()
                gl.checkError { "" }
            }
            gl.checkError { "" }
        } else {
            frameBuffer.enable()
            gl.checkError { "" }
            frameBuffer.bind()
            gl.checkError { "" }
        }
    }

    fun end() {
        if (isClosed)
            throw IllegalStateException("Object is closed")
        if (msaa != TextureObject.MSAALevels.Disable) {
            msaaFrameBuffer?.bind(draw = false, read = true)
            frameBuffer.bind(draw = true, read = false)
            gl.glBlitFramebuffer(0, 0, width, height, 0, 0, width, height, gl.COLOR_BUFFER_BIT, gl.NEAREST)
            frameBuffer.unbind(draw = true, read = false)
        }
        msaaFrameBuffer?.unbind()
        msaaFrameBuffer?.unbind(draw = false, read = true)
        frameBuffer.unbind()
        frameBuffer.disable()
    }

    fun getGlTexture() = frameBuffer.texture?.glTexture
    fun getGlTextureTarget() = frameBuffer.texture?.target

    override fun close() {
        msaaFrameBuffer?.close()
        frameBuffer.close()
        isClosed = true
    }

    fun resize(w: Int, h: Int) {
        msaaFrameBuffer?.resize(w, h)
        frameBuffer.resize(w, h)
    }
}