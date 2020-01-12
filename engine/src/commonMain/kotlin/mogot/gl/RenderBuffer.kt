package mogot.gl

import pw.binom.io.Closeable

class RenderBuffer(val gl: GL, val width:Int,val height:Int,format:RenderBufferFormat) : Closeable {
    val rbo = gl.createRenderBuffer()
    init {
        bind()
        //if(multisample == TextureObject.MSAALevels.MSAADisable)
        gl.renderbufferStorage(gl.RENDERBUFFER, getRenderBufferFormat(gl,format),width,height)

        //else{

        //}
        unbind()
    }
    fun bind(){
        gl.bindRenderBuffer(gl.RENDERBUFFER, rbo)
    }
    fun unbind(){
        gl.bindRenderBuffer(gl.RENDERBUFFER,null)
    }
    override fun close() {
        gl.deleteBuffer(rbo)
    }

}