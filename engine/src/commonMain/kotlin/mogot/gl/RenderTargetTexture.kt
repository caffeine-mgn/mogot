package mogot.gl

class RenderTargetTexture(val gl: GL, width:Int,height:Int) {
    val frameBuffer = FrameBuffer(gl,
            TextureObject(gl,width,height,TextureObject.FilterParameter.Nearest,TextureObject.FilterParameter.Nearest,TextureObject.TextureWrap.ClampToEdge,TextureObject.TextureWrap.ClampToEdge,TextureObject.MSAALevels.Disable),
            RenderBuffer(gl,width,height,RenderBufferFormat.DEPTH24_STENCIL8))
    fun begin(){
        frameBuffer.enable()
        frameBuffer.bind()
    }

    fun end(){
        frameBuffer.unbind()
        frameBuffer.disable()
    }

    fun getGlTexture() = frameBuffer.texture?.glTexture
    fun getGlTextureTarget() = frameBuffer.texture?.target
}