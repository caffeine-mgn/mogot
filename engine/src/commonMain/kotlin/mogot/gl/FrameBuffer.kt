package mogot.gl

import mogot.Engine
import pw.binom.io.Closeable

class FrameBuffer(val engine: Engine,val msaaLevel:MSAALevels) :Closeable{
    private var fbo: GLFrameBuffer? = null
    private val textureTarget = when(msaaLevel){
        MSAALevels.MSAADisable -> engine.gl.TEXTURE_2D
        else -> engine.gl.TEXTURE_2D_MULTISAMPLE
    }
    enum class MSAALevels(val level:Int){
        MSAADisable(0),
        MSAAx2(2),
        MSAAx4(4),
        MSAAx8(8),
        MSAAx16(16)
    }
    fun init(widh:Int,height:Int){
        val gl = engine.gl
        fbo = gl.createFrameBuffer()
    }
    override fun close(){
        bind()
        fbo?.let {
            engine.gl.deleteBuffer(it)
        }
        unbind()
    }
    fun bind(){
        val gl = engine.gl
        gl.enable(textureTarget)
        if(msaaLevel != MSAALevels.MSAADisable)
            gl.enable(gl.MULTISAMPLE)
        gl.bindFrameBuffer(gl.FRAMEBUFFER,fbo)
    }
    fun unbind(){
        val gl = engine.gl
        gl.disable(textureTarget)
        if(msaaLevel != MSAALevels.MSAADisable)
            gl.disable(gl.MULTISAMPLE)
        gl.bindFrameBuffer(gl.FRAMEBUFFER,null)
    }

    fun bindTexture(glTexture: GLTexture){
        val gl = engine.gl
        gl.framebufferTexture2D(gl.FRAMEBUFFER, gl.COLOR_ATTACHMENT0, textureTarget, glTexture, 0)
    }

    fun check(){
        val gl = engine.gl
        if(gl.checkFramebufferStatus(gl.FRAMEBUFFER)!=gl.FRAMEBUFFER_COMPLETE){

        }
    }
}