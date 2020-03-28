package mogot.rendering

import mogot.*
import mogot.gl.*

class CanvasRenderPass(engine: Engine) : TextureRenderPass() {
    init {
        outputRenderPassData.values["stage"] = CanvasRenderPass::class.simpleName!!
    }
    val sprite = FullScreenSprite(engine)
    private val mat = FullScreenMaterial(engine)
    override fun render(renderContext: RenderContext, gl: GL, camera: Camera?, camera2D: Camera2D?, root: Node, dt: Float, inputRenderPassData: RenderPassData): RenderPassData {
        resize(inputRenderPassData,gl)
        val texture = inputRenderPassData.values[RenderPassData.RENDER_TARGET_TEXTURE] as RenderTargetTexture
        requireNotNull(renderTargetTexture).apply {
            begin()
            gl.clear(gl.COLOR_BUFFER_BIT)
            mat.texture2D = texture.getGlTexture()
            sprite.material = mat
            gl.bindTexture(texture.getGlTextureTarget()!!, texture.getGlTexture())
            sprite.draw(renderContext)
            gl.bindTexture(texture.getGlTextureTarget()!!, null)
            end()
            outputRenderPassData.values[RenderPassData.WIDTH] = width
            outputRenderPassData.values[RenderPassData.HEIGHT] = height
            outputRenderPassData.values[RenderPassData.RENDER_TARGET_TEXTURE] = this
        }

        return super.render(renderContext, gl, camera, camera2D, root, dt, outputRenderPassData)
    }
}