package mogot.gl

import mogot.Sprite

class PostEffectPipeline(val gl: GL, var resolutionWidth: Int, var resolutionHeight: Int) {
    private val list = mutableListOf<SimplePostEffect>()
    private val sprite: Sprite = Sprite(gl).apply { material = object :MaterialGLSL(gl){
        override val shader: Shader
            get() = Shader(gl,"```test```","```test```")

        override fun close() {
            shader.close()
        }

    } }
    private var texture: GLTexture? = null

    var fbo:Int? = null
    var rbo:Int? = null

    fun close(){
        fbo?.let {
            gl.deleteBuffers(it)
        }
        gl.bindTexture(gl.TEXTURE_2D, null)
        rbo?.let {
            gl.deleteBuffers(rbo!!)
        }
    }

    fun addEffect(effect:SimplePostEffect){
        if(!list.contains(effect))
            list.add(effect)
    }

    fun use(drawScene:()->Unit){
        gl.bindFramebuffer(gl.FRAMEBUFFER,checkNotNull(fbo){"Frame buffer not created"})
        list.forEach { currentFrameEffect->
            sprite.material = currentFrameEffect
            gl.bindFramebuffer(gl.FRAMEBUFFER,checkNotNull(fbo){"Frame buffer not created"})
            gl.clear(gl.GL_COLOR_BUFFER_BIT or gl.GL_DEPTH_BUFFER_BIT)
            gl.enable(gl.DEPTH_TEST)
            gl.enable(gl.CULL_FACE)
            drawScene()
            gl.bindFramebuffer(gl.FRAMEBUFFER,0)
        }
        gl.clear(gl.COLOR_BUFFER_BIT)
        gl.bindTexture(gl.TEXTURE_2D, checkNotNull(texture))
        sprite.material
        gl.bindTexture(gl.TEXTURE_2D, null)
    }
    


    fun init(){
        fbo = gl.genFramebuffers()
        gl.bindFramebuffer(gl.FRAMEBUFFER,fbo!!)
        texture = gl.createTexture()
        gl.bindTexture(gl.TEXTURE_2D, texture!!)
        gl.texImage2D(gl.TEXTURE_2D, 0, gl.RGB, resolutionWidth, resolutionHeight, 0, gl.RGB, gl.UNSIGNED_BYTE, 0)
        gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.LINEAR)
        gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MAG_FILTER, gl.LINEAR)
        gl.bindTexture(gl.TEXTURE_2D, null)
        gl.framebufferTexture2D(gl.FRAMEBUFFER, gl.COLOR_ATTACHMENT0, gl.TEXTURE_2D, texture!!, 0)
        rbo = gl.genRenderbuffers()
        gl.bindRenderbuffer(gl.RENDERBUFFER, rbo!!)
        gl.renderbufferStorage(gl.RENDERBUFFER,gl.DEPTH24_STENCIL8, resolutionWidth,resolutionHeight)
        gl.bindRenderbuffer(gl.RENDERBUFFER, 0)
        gl.framebufferRenderbuffer(gl.FRAMEBUFFER, gl.DEPTH_STENCIL_ATTACHMENT, gl.RENDERBUFFER, rbo!!)
        if(gl.checkFramebufferStatus(gl.FRAMEBUFFER) != gl.FRAMEBUFFER_COMPLETE)
        //LOG.log(Level.SEVERE,"ERROR::FRAMEBUFFER:: Framebuffer is not complete!")
            println("ERROR::FRAMEBUFFER:: Framebuffer is not complete!")
        gl.bindFramebuffer(gl.FRAMEBUFFER, 0)
    }

}