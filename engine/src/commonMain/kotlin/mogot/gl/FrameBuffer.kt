package mogot.gl

import mogot.Engine
import pw.binom.io.Closeable

class FrameBuffer(val engine: Engine,val widh:Int,val height:Int) :Closeable{
    private var fbo: GLFrameBuffer? = null

    init{
        val gl = engine.gl
        fbo = gl.createFrameBuffer()
    }
    override fun close(){
        fbo?.let {
            engine.gl.deleteBuffer(it)
        }
    }
    fun bind(){
        val gl = engine.gl
        gl.bindFrameBuffer(gl.FRAMEBUFFER,fbo)
    }
    fun unbind(){
        val gl = engine.gl
        gl.bindFrameBuffer(gl.FRAMEBUFFER,null)
    }

    fun bindTexture(texture:TextureObject){
        val gl = engine.gl
        gl.framebufferTexture2D(gl.FRAMEBUFFER, gl.COLOR_ATTACHMENT0, texture.target, texture.glTexture, 0)
    }

    fun check(){
        val gl = engine.gl
        if(gl.checkFramebufferStatus(gl.FRAMEBUFFER)!=gl.FRAMEBUFFER_COMPLETE){

        }
    }
}