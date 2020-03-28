package mogot.gl

import mogot.Engine
import mogot.ResourceHolder
import mogot.ResourceImpl
import mogot.math.MATRIX4_ONE
import mogot.math.Matrix4f
import mogot.math.Matrix4fc
import mogot.rendering.Display
import pw.binom.floatDataOf
import pw.binom.intDataOf
import pw.binom.io.Closeable

open class ScreenRect(val gl: GL) : Closeable {
    private val vertexBuffer = BufferArray(gl = gl, static = true, draw = true).apply {
        uploadArray(floatDataOf(
                -1f, 1f, -0.0f,
                -1f, -1f, -0.0f,
                1f, -1f, -0.0f,
                1f, 1f, -0.0f
        ))
    }
    private val uvBuffer = BufferArray(gl = gl, static = true, draw = true).apply {
        uploadArray(floatDataOf(0f, 1f, 0f, 0f, 1f, 0f, 1f, 1f))
    }
    private val vao = VertexArray(gl)
    private val vertexSize = 12 / 3
    private val indexBuffer = BufferElementArray(gl, static = true, draw = true)

    init {
        vao.bind {
            indexBuffer.uploadArray(intDataOf(0, 1, 3, 3, 1, 2))
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

class FullScreenMaterial(gl: GL) : MaterialGLSL(gl) {
    override val shader: Shader
        get() = Shader(gl, """#version 450 core
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

    override fun use(model: Matrix4fc, projection: Matrix4fc, context: Display.Context) {
        super.use(model, projection, context)
        if (texture2D != null) {
            gl.activeTexture(gl.TEXTURE0)
            gl.bindTexture(gl.TEXTURE_2D, texture2D!!)
            shader.use()
            shader.uniform("screenTexture", 0)

        }
    }

    override fun unuse() {
        if (texture2D != null) {
            gl.activeTexture(gl.TEXTURE0)
            gl.bindTexture(gl.TEXTURE_2D, null)
        }
        super.unuse()
    }
}

class FullScreenSprite(gl: GL): ResourceImpl() {
    //    var defaultMaterial = FullScreenMaterial(engine)
    var material by ResourceHolder<MaterialGLSL>(FullScreenMaterial(gl))
    private val rect = ScreenRect(gl)

    fun draw(context: Display.Context) {
        val mat = material ?: TODO()
        mat.use(MATRIX4_ONE, Matrix4f().identity(), context)
        rect.draw()
        mat.unuse()
    }

    override fun dispose() {
        material = null
        super.dispose()
    }
}

@Deprecated("Will be removed in future")
class PostEffectPipeline(val engine: Engine): ResourceImpl() {
    private var isClosed: Boolean = false
    private val gl
        get() = engine.gl
    val effects = mutableListOf<SimplePostEffect>()
    private var sprite by ResourceHolder<FullScreenSprite>()

    var MSAALevel: TextureObject.MSAALevels = TextureObject.MSAALevels.Disable

    private var renderTargetTexture: RenderTargetTexture? = null

    fun close() {
        sprite = null
        renderTargetTexture?.close()
        renderTargetTexture = null
        isClosed = true
    }

    fun addEffect(effect: SimplePostEffect) {
        if (!effects.contains(effect))
            effects.add(effect)
    }

    private val mat = FullScreenMaterial(gl)

    fun begin(){
        if(isClosed) throw IllegalStateException("Object is closed")
        val renderTargetTexture = requireNotNull(renderTargetTexture)
        renderTargetTexture.begin()
        gl.clear(gl.COLOR_BUFFER_BIT or gl.DEPTH_BUFFER_BIT)
    }

    fun end(context: Display.Context){
        if(isClosed) throw IllegalStateException("Object is closed")
        renderTargetTexture?.end()
        effects.forEach { currentFrameEffect ->
            gl.bindTexture(renderTargetTexture!!.getGlTextureTarget()!!, renderTargetTexture!!.getGlTexture())
            renderTargetTexture!!.begin()
            gl.clear(gl.COLOR_BUFFER_BIT)
            sprite?.material = currentFrameEffect
            sprite?.draw(context)
            renderTargetTexture!!.end()
            gl.bindTexture(renderTargetTexture!!.getGlTextureTarget()!!, null)
        }
        gl.bindFrameBuffer(gl.FRAMEBUFFER, null)
        gl.clear(gl.COLOR_BUFFER_BIT)
        mat.texture2D = renderTargetTexture!!.getGlTexture()
        sprite?.material = mat
        gl.activeTexture(engine.gl.TEXTURE0)
        gl.bindTexture(renderTargetTexture!!.getGlTextureTarget()!!, renderTargetTexture!!.getGlTexture())
        sprite?.draw(context)
        gl.bindTexture(renderTargetTexture!!.getGlTextureTarget()!!, null)
    }

    fun init(resolutionWidth: Int, resolutionHeight: Int) {
        isClosed = false
        renderTargetTexture = RenderTargetTexture(engine.gl, resolutionWidth, resolutionHeight,MSAALevel)
        sprite = FullScreenSprite(gl)
    }

    override fun dispose() {
        close()
    }


}