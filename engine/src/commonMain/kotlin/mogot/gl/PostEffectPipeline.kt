package mogot.gl

import mogot.Engine
import mogot.ResourceHolder
import mogot.ResourceImpl
import mogot.rendering.Display

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