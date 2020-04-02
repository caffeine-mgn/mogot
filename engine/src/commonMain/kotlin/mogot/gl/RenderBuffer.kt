package mogot.gl

import pw.binom.io.Closeable

class RenderBuffer(val gl: GL, val width:Int,val height:Int,format:RenderBufferFormat,val msaa: TextureObject.MSAALevels) : Closeable {
    val rbo = gl.createRenderBuffer()
    init {
        bind()
        gl.checkError{""}
        if(msaa == TextureObject.MSAALevels.Disable)
            gl.renderbufferStorage(gl.RENDERBUFFER, getRenderBufferFormat(gl,format),width,height)
        else{
            gl.renderbufferStorageMultisample(gl.RENDERBUFFER,msaa.level,getRenderBufferFormat(gl,format),width,height)
        }
        gl.checkError{""}
        unbind()
        gl.checkError{""}
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