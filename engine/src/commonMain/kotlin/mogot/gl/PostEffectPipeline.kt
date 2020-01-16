package mogot.gl

import mogot.Engine
import mogot.Material
import mogot.RenderContext
import mogot.math.MATRIX4_ONE
import mogot.math.Matrix4f
import mogot.math.Matrix4fc
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

class FullScreenMaterial(engine: Engine) : MaterialGLSL(engine) {
    override val shader: Shader
        get() = Shader(engine.gl, """#version 450 core
                                        layout (location = 0) in vec3 aPos;
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

    var texture2D: GLTexture? = null

    override fun use(model: Matrix4fc, projection: Matrix4fc, renderContext: RenderContext) {
        super.use(model, projection, renderContext)
        if (texture2D != null) {
            engine.gl.activeTexture(engine.gl.TEXTURE0)
            engine.gl.bindTexture(engine.gl.TEXTURE_2D, texture2D!!)
            shader.uniform("screenTexture", 0)
        }
    }

    override fun unuse() {
        if (texture2D != null) {
            engine.gl.activeTexture(engine.gl.TEXTURE0)
            engine.gl.bindTexture(engine.gl.TEXTURE_2D, null)
        }
        super.unuse()
    }
}

class FullScreenSprite(engine: Engine) {
    //    var defaultMaterial = FullScreenMaterial(engine)
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
        val mat = material ?: TODO()
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

    var MSAAEnabled = false

    var MSAALevel: TextureObject.MSAALevels = TextureObject.MSAALevels.Disable

    private var renderTargetTexture: RenderTargetTexture? = null

    fun close() {

    }

    fun addEffect(effect: SimplePostEffect) {
        if (!list.contains(effect))
            list.add(effect)
    }

    private val mat = FullScreenMaterial(engine)
    fun use(renderContext: RenderContext, drawScene: () -> Unit) {
        val renderTargetTexture = requireNotNull(renderTargetTexture)
        //gl.enable(gl.DEPTH_TEST)
        //gl.enable(gl.CULL_FACE)
        renderTargetTexture.begin()
        gl.clear(gl.COLOR_BUFFER_BIT or gl.DEPTH_BUFFER_BIT)
        drawScene()
        renderTargetTexture.end()

       ///////
        //gl.disable(gl.DEPTH_TEST)
        //gl.disable(gl.CULL_FACE)
        ///////

        list.forEach { currentFrameEffect ->
            gl.bindTexture(renderTargetTexture.getGlTextureTarget()!!, renderTargetTexture.getGlTexture())
            renderTargetTexture.begin()
            gl.clear(gl.COLOR_BUFFER_BIT)
            sprite.material = currentFrameEffect
            sprite.draw(renderContext)
            renderTargetTexture.end()
            gl.bindTexture(renderTargetTexture.getGlTextureTarget()!!, null)
        }
        gl.bindFrameBuffer(gl.FRAMEBUFFER, null)
        gl.clear(gl.COLOR_BUFFER_BIT)
        mat.texture2D = renderTargetTexture.getGlTexture()
        sprite.material = mat
        gl.activeTexture(engine.gl.TEXTURE0)
        gl.bindTexture(renderTargetTexture.getGlTextureTarget()!!, renderTargetTexture.getGlTexture())

        sprite.draw(renderContext)
        gl.bindTexture(renderTargetTexture.getGlTextureTarget()!!, null)
    }


    fun init(resolutionWidth: Int, resolutionHeight: Int) {
        renderTargetTexture = RenderTargetTexture(engine.gl, resolutionWidth, resolutionHeight)
    }

}