package mogot.gl

import pw.binom.io.Closeable

class FrameBuffer(val gl: GL,val texture: TextureObject? = null, val renderBuffer: RenderBuffer? = null) :Closeable{
    private var fbo: GLFrameBuffer? = null

    init{
        fbo = gl.createFrameBuffer()
        bind()
        if(texture!=null) {
            gl.framebufferTexture2D(gl.FRAMEBUFFER, if(texture.format == TextureObject.Format.RGB) gl.COLOR_ATTACHMENT0 else if(texture.format == TextureObject.Format.DEPTH_COMPONENT) gl.DEPTH_ATTACHMENT else TODO(), texture.target, texture.glTexture, 0)
            if(texture.format==TextureObject.Format.DEPTH_COMPONENT){
                gl.drawBuffer(gl.NONE)
                gl.readBuffer(gl.NONE)
            }
            gl.checkError {
                "Framebuffer texture error"
            }
        }
        if(renderBuffer!=null){
            gl.framebufferRenderbuffer(gl.FRAMEBUFFER,gl.DEPTH_STENCIL_ATTACHMENT,gl.RENDERBUFFER,renderBuffer.rbo)
            gl.checkError {
                "Framebuffer renderbuffer error"
            }
        }
        check()
        unbind()
    }
    override fun close(){
        fbo?.let {
            gl.deleteBuffer(it)
            texture?.close()
            renderBuffer?.close()
        }
    }
    fun bind(read:Boolean = true, draw:Boolean = true){
        if(read && draw)
            gl.bindFrameBuffer(gl.FRAMEBUFFER,fbo)
        else if(read){
            gl.bindFrameBuffer(gl.READ_FRAMEBUFFER,fbo)
        }else{
            gl.bindFrameBuffer(gl.DRAW_FRAMEBUFFER,fbo)
        }
    }
    fun unbind(read:Boolean = true, draw:Boolean = true){
        if(read && draw)
            gl.bindFrameBuffer(gl.FRAMEBUFFER,null)
        else if(read){
            gl.bindFrameBuffer(gl.READ_FRAMEBUFFER,null)
        }else{
            gl.bindFrameBuffer(gl.DRAW_FRAMEBUFFER,null)
        }
    }

    fun check(){
        if(gl.checkFramebufferStatus(gl.FRAMEBUFFER)!=gl.FRAMEBUFFER_COMPLETE){
            println("FrameBuffer not complected")
        }
    }

    fun enable(){
        texture?.enable()
    }

    fun disable(){
        texture?.disable()
    }
}