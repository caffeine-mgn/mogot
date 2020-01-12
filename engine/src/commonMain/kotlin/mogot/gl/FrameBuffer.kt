package mogot.gl

import pw.binom.io.Closeable

class FrameBuffer(val gl: GL,val texture: TextureObject? = null, val renderBuffer: RenderBuffer? = null) :Closeable{
    private var fbo: GLFrameBuffer? = null

    init{
        val gl = gl
        fbo = gl.createFrameBuffer()
        bind()
        if(texture!=null) {
            gl.framebufferTexture2D(gl.FRAMEBUFFER, gl.COLOR_ATTACHMENT0, texture.target, texture.glTexture, 0)
        }
        if(renderBuffer!=null){
            gl.framebufferRenderbuffer(gl.FRAMEBUFFER,gl.DEPTH_STENCIL_ATTACHMENT,gl.RENDERBUFFER,renderBuffer.rbo)
        }
        unbind()
    }
    override fun close(){
        fbo?.let {
            gl.deleteBuffer(it)
            texture?.close()
            renderBuffer?.close()
        }
    }
    fun bind(){
        val gl = gl
        gl.bindFrameBuffer(gl.FRAMEBUFFER,fbo)
    }
    fun unbind(){
        val gl = gl
        gl.bindFrameBuffer(gl.FRAMEBUFFER,null)
    }

    fun check(){
        val gl = gl
        if(gl.checkFramebufferStatus(gl.FRAMEBUFFER)!=gl.FRAMEBUFFER_COMPLETE){


        }
    }

    fun enable(){
        texture?.enable()
    }

    fun disable(){
        texture?.disable()
    }
}