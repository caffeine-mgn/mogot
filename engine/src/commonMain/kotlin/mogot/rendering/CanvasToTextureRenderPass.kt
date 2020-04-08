package mogot.rendering

import mogot.*
import mogot.gl.*

class CanvasToTextureRenderPass() : ToTextureRenderPass() {
    init {
        outputRenderPassData.values["stage"] = CanvasToTextureRenderPass::class.simpleName!!
    }
    private var sprite:FullScreenSprite? = null
    private var mat:FullScreenMaterial? = null
    override fun render(context: Display.Context, gl: GL, root: Node, dt: Float, inputRenderPassData: RenderPassData): RenderPassData {
        val texture = inputRenderPassData.values[RenderPassData.RENDER_TARGET_TEXTURE] as RenderTargetTexture
        if(bypass){
            outputRenderPassData.values[RenderPassData.RENDER_TARGET_TEXTURE] = texture
        }else {
            requireNotNull(renderTargetTexture).apply {
                begin()
                gl.checkError{""}
                gl.clear(gl.COLOR_BUFFER_BIT)
                mat?.texture2D = texture.getGlTexture()
                sprite?.material = mat
                //gl.bindTexture(texture.getGlTextureTarget()!!, texture.getGlTexture())
                sprite?.draw(context)
                //gl.bindTexture(texture.getGlTextureTarget()!!, null)
                if(context.camera2D!=null) {
                    //camera2D?.globalToLocalMatrix(cameraModel2DMatrix.identity())?: cameraModel2DMatrix.identity()
                    customPreDraw2D(context.camera2D!!.projectionMatrix, context)
                    context.renderNode2D(root, context.camera2D!!.projectionMatrix, context)
                    gl.checkError{""}
                }
                end()
                gl.checkError{""}
                outputRenderPassData.values[RenderPassData.RENDER_TARGET_TEXTURE] = this
            }
        }
        return super.render(context, gl, root, dt, outputRenderPassData)
    }

    override fun cleanup() {
        sprite?.dec()
        mat?.dec()
        super.cleanup()
    }

    override fun setup(context: Display.Context, gl: GL, msaaLevel: TextureObject.MSAALevels) {
        super.setup(context, gl, msaaLevel)
        if(sprite==null)
            sprite = FullScreenSprite(gl)
        if(mat == null)
            mat = FullScreenMaterial(gl)
    }
}