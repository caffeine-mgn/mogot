package mogot.rendering

import mogot.*
import mogot.gl.*
import mogot.math.Matrix4fc

class CanvasRenderPass(nextPass:RenderPass) : ToTextureRenderPass(nextPass) {
    init {
        outputRenderPassData.values["stage"] = CanvasRenderPass::class.simpleName!!
    }
    private var sprite:FullScreenSprite? = null
    private var mat:FullScreenMaterial? = null
    override fun render(context: Display.Context, gl: GL, camera: Camera?, camera2D: Camera2D?, root: Node, dt: Float, inputRenderPassData: RenderPassData): RenderPassData {
        resize(inputRenderPassData,gl)
        if(sprite==null)
            sprite = FullScreenSprite(gl)
        if(mat == null)
            mat = FullScreenMaterial(gl)
        val texture = inputRenderPassData.values[RenderPassData.RENDER_TARGET_TEXTURE] as RenderTargetTexture
        if(bypass){
            outputRenderPassData.values[RenderPassData.WIDTH] = width
            outputRenderPassData.values[RenderPassData.HEIGHT] = height
            outputRenderPassData.values[RenderPassData.RENDER_TARGET_TEXTURE] = texture
        }else {
            requireNotNull(renderTargetTexture).apply {
                begin()
                gl.clear(gl.COLOR_BUFFER_BIT)
                mat.texture2D = texture.getGlTexture()
                sprite.material = mat
                gl.bindTexture(texture.getGlTextureTarget()!!, texture.getGlTexture())
                sprite.draw(context)
                gl.bindTexture(texture.getGlTextureTarget()!!, null)
                if(camera2D!=null)
                    renderNode2D(root,camera2D.projectionMatrix,context)
                end()
                outputRenderPassData.values[RenderPassData.WIDTH] = width
                outputRenderPassData.values[RenderPassData.HEIGHT] = height
                outputRenderPassData.values[RenderPassData.RENDER_TARGET_TEXTURE] = this
            }
        }
        return super.render(context, gl, camera, camera2D, root, dt, outputRenderPassData)
    }

    private fun renderNode2D(node: Node, projection: Matrix4fc, context: Display.Context) {
        if (node.isVisualInstance2D()) {
            if (!node.visible)
                return
            node.render(node.matrix, projection, context)
        }


        node.childs.forEach {
            renderNode2D(it, projection, context)
        }
    }
}