package mogot.gl

import mogot.Engine
import mogot.Material
import mogot.RenderContext
import mogot.math.MATRIX4_ONE
import mogot.math.Matrix4f
import pw.binom.io.Closeable

open class ScreenRect(val gl: GL) : Closeable {
    private val vertexBuffer = BufferArray(gl = gl, static = true, draw = true).apply {
        uploadArray(floatArrayOf(
                -1f, 1f, -0.0f,
                -1f, -1f, -0.0f,
                1f, -1f, -0.0f,
                1f, 1f, -0.0f
        ))
    }
    private val uvBuffer = BufferArray(gl = gl, static = true, draw = true).apply {
        uploadArray(floatArrayOf(0f, 1f, 0f, 0f, 1f, 0f, 1f, 1f))
    }
    private val vao = VertexArray(gl)
    private val vertexSize = 12 / 3
    private val indexBuffer = BufferElementArray(gl, static = true, draw = true)

    init {
        vao.bind {
            indexBuffer.uploadArray(intArrayOf(0, 1, 3, 3, 1, 2))
            indexBuffer.bind()

            vertexBuffer.bind()
            gl.vertexAttribPointer(0, 3, gl.FLOAT, false, 0, 0)
            gl.enableVertexAttribArray(0)

            uvBuffer.bind()
            gl.vertexAttribPointer(2, 2, gl.FLOAT, false, 0, 0)
            gl.enableVertexAttribArray(2)

        }
    }

    fun draw() {
        vao.bind {
            gl.drawElements(gl.TRIANGLES, 6, gl.UNSIGNED_INT, 0)
        }
    }

    override fun close() {
        vao.close()
        vertexBuffer.close()
        uvBuffer.close()
    }
}

class FullScreenSprite(engine: Engine) {
    var defaultMaterial = object : MaterialGLSL(engine) {
        override val shader: Shader
            get() = Shader(engine.gl, """#version 450 core
                                        layout (location = 0) in vec2 aPos;
                                        layout (location = 2) in vec2 aTexCoords;
                                        out vec2 TexCoords;
                                        void main()
                                        {
                                            gl_Position = vec4(aPos.x, aPos.y, 0.0, 1.0);
                                            TexCoords = aTexCoords;
                                        }""",
                    """#version 450 core
                                out vec4 FragColor;
                                in vec2 TexCoords;
                                uniform sampler2D screenTexture;
                                void main()
                                { 
                                    FragColor = texture(screenTexture, TexCoords);
                                }""")

        override fun dispose() {
            shader.close()
            super.dispose()
        }
    }
    var material: Material? = object : MaterialGLSL(engine) {
        val defaulTshader: Shader
            get() = Shader(engine.gl, """#version 450 core
                                        layout (location = 0) in vec2 aPos;
                                        layout (location = 2) in vec2 aTexCoords;
                                        out vec2 TexCoords;
                                        void main()
                                        {
                                            gl_Position = vec4(aPos.x, aPos.y, 0.0, 1.0);
                                            TexCoords = aTexCoords;
                                        }""",
                    """#version 450 core
                                out vec4 FragColor;
                                in vec2 TexCoords;
                                uniform sampler2D screenTexture;
                                void main()
                                { 
                                    FragColor = texture(screenTexture, TexCoords);
                                }""")
        override val shader: Shader
            get() = Shader(engine.gl, """#version 450 core
                                        layout (location = 0) in vec2 aPos;
                                        layout (location = 2) in vec2 aTexCoords;
                                        out vec2 TexCoords;
                                        void main()
                                        {
                                            gl_Position = vec4(aPos.x, aPos.y, 0.0, 1.0);
                                            TexCoords = aTexCoords;
                                        }""",
                    """#version 450 core
                                out vec4 FragColor;
                                in vec2 TexCoords;
                                uniform sampler2D screenTexture;
                                void main()
                                { 
                                    FragColor = texture(screenTexture, TexCoords);
                                }""")

        override fun dispose() {
            shader.close()
            super.dispose()
        }
    }
    private val rect = ScreenRect(engine.gl)

    fun draw(renderContext: RenderContext) {
        val mat = material ?: return
        mat.use(MATRIX4_ONE, Matrix4f().identity(), renderContext)
        rect.draw()
        mat.unuse()
    }
}

class PostEffectPipeline(val engine: Engine) {
    private val gl
        get() = engine.gl
    private val list = mutableListOf<SimplePostEffect>()
    private val sprite: FullScreenSprite = FullScreenSprite(engine)
    private var texture: GLTexture? = null

    var MSAAEnabled = true

    var MSAALevel: FrameBuffer.MSAALevels = FrameBuffer.MSAALevels.MSAAx4


    var fbo: GLFrameBuffer? = null
    var rbo: GLRenderBuffer? = null

    fun close() {
        val texture2dTarget = if(MSAAEnabled) gl.TEXTURE_2D_MULTISAMPLE else gl.TEXTURE_2D
        fbo?.let {
            gl.deleteBuffer(it)
            fbo = null
        }
        gl.bindTexture(texture2dTarget, null)
        rbo?.let {
            gl.deleteBuffer(rbo!!)
            rbo = null
        }
        /*texture?.let {
            gl.deleteTexture(it)
            texture = null
        }*/
    }

    fun addEffect(effect: SimplePostEffect) {
        if (!list.contains(effect))
            list.add(effect)
    }

    fun use(renderContext: RenderContext, drawScene: () -> Unit) {
        val texture2dTarget = if(MSAAEnabled) gl.TEXTURE_2D_MULTISAMPLE else gl.TEXTURE_2D
        gl.enable(gl.DEPTH_TEST)
        gl.enable(gl.CULL_FACE)
        gl.bindFrameBuffer(gl.FRAMEBUFFER, checkNotNull(fbo) { "Frame buffer not created" })
        gl.clear(gl.COLOR_BUFFER_BIT or gl.DEPTH_BUFFER_BIT)

        drawScene()
        //gl.bindFramebuffer(gl.FRAMEBUFFER,0)
        gl.disable(gl.DEPTH_TEST)
        gl.disable(gl.CULL_FACE)

        list.forEach { currentFrameEffect ->
            gl.bindTexture(texture2dTarget, checkNotNull(texture))
            gl.clear(gl.COLOR_BUFFER_BIT)
            sprite.material = currentFrameEffect
            sprite.draw(renderContext)
        }
        gl.bindFrameBuffer(gl.FRAMEBUFFER, null)
        gl.clear(gl.COLOR_BUFFER_BIT)
        sprite.material = sprite.defaultMaterial
        gl.bindTexture(texture2dTarget, checkNotNull(texture))
        sprite.draw(renderContext)
        gl.bindTexture(texture2dTarget, null)
    }


    fun init(resolutionWidth: Int, resolutionHeight: Int) {
        fbo = gl.createFrameBuffer()
        gl.bindFrameBuffer(gl.FRAMEBUFFER, fbo!!)
        val texture2dTarget = if(MSAAEnabled) gl.TEXTURE_2D_MULTISAMPLE else gl.TEXTURE_2D
        gl.enable(texture2dTarget)
        gl.enable(gl.MULTISAMPLE)
        texture = gl.createTexture()
        gl.bindTexture(texture2dTarget, texture!!)
        if(!MSAAEnabled) {
            gl.texImage2D(gl.TEXTURE_2D, 0, gl.RGB, resolutionWidth, resolutionHeight, 0, gl.RGB, gl.UNSIGNED_BYTE, null)
            gl.texParameteri(texture2dTarget, gl.TEXTURE_MIN_FILTER, gl.NEAREST)
            gl.texParameteri(texture2dTarget, gl.TEXTURE_MAG_FILTER, gl.NEAREST)
        }
        else {
            gl.texImage2DMultisample(gl.TEXTURE_2D_MULTISAMPLE,MSAALevel.level,  gl.RGB ,resolutionWidth, resolutionHeight,false)
        }

        //gl.texParameterf(gl.TEXTURE_2D, gl.MAX_TEXTURE_MAX_ANISOTROPY_EXT,0.0f)

        gl.framebufferTexture2D(gl.FRAMEBUFFER, gl.COLOR_ATTACHMENT0, texture2dTarget, texture!!, 0)
        rbo = gl.createRenderBuffer()
        gl.bindRenderBuffer(gl.RENDERBUFFER, rbo!!)
        if(MSAAEnabled){
            gl.renderbufferStorageMultisample(gl.RENDERBUFFER,MSAALevel.level,gl.DEPTH24_STENCIL8,resolutionWidth,resolutionHeight)
        }else {
            gl.renderbufferStorage(gl.RENDERBUFFER, gl.DEPTH24_STENCIL8, resolutionWidth, resolutionHeight)
        }
        gl.bindTexture(texture2dTarget, null)
        gl.bindRenderBuffer(gl.RENDERBUFFER, null)
        gl.framebufferRenderbuffer(gl.FRAMEBUFFER, gl.DEPTH_STENCIL_ATTACHMENT, gl.RENDERBUFFER, rbo!!)
        if (gl.checkFramebufferStatus(gl.FRAMEBUFFER) != gl.FRAMEBUFFER_COMPLETE)
        //LOG.log(Level.SEVERE,"ERROR::FRAMEBUFFER:: Framebuffer is not complete!")
            println("ERROR::FRAMEBUFFER:: Framebuffer is not complete!")
        gl.bindFrameBuffer(gl.FRAMEBUFFER, null)
    }

}