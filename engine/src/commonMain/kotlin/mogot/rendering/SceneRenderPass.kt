package mogot.rendering

import mogot.*
import mogot.gl.GL
import mogot.gl.RenderTargetTexture
import mogot.gl.TextureObject
import mogot.math.Matrix4f
import mogot.math.Matrix4fc

class SceneRenderPass : BaseRenderPass() {
    private var width:Int = 0
    private var height:Int = 0
    private var renderTargetTexture: RenderTargetTexture? = null
    private val cameraModel3DMatrix = Matrix4f()
    //private val cameraModel2DMatrix = Matrix4f()

    init {
        outputRenderPassData.values["stage"] = SceneRenderPass::class.simpleName!!
    }
    override fun render(renderContext: RenderContext, gl: GL, camera: Camera?, camera2D: Camera2D?, root: Node, dt: Float, inputRenderPassData: RenderPassData): RenderPassData {
        val w = inputRenderPassData.values["width"] as Int
        val h = inputRenderPassData.values["height"] as Int
        if((w!=width)||(h!=height)){
            width = w
            height = h
            renderTargetTexture?.close()
            val msaaParam = inputRenderPassData.values["msaa"]
            var msaa = TextureObject.MSAALevels.Disable
            msaaParam?.let {
                msaa = when(it){
                    "4" -> TextureObject.MSAALevels.MSAAx4
                    "16" -> TextureObject.MSAALevels.MSAAx16
                    "8" -> TextureObject.MSAALevels.MSAAx8
                    else -> TextureObject.MSAALevels.Disable
                }
            }
            renderTargetTexture = RenderTargetTexture(gl,width,height,msaa)
        }
        requireNotNull(renderTargetTexture).apply {
            camera?.globalToLocalMatrix(cameraModel3DMatrix.identity())
            begin()
            gl.clear(gl.COLOR_BUFFER_BIT or gl.DEPTH_BUFFER_BIT)
            gl.enable(gl.DEPTH_TEST)
            gl.enable(gl.CULL_FACE)
            if (camera != null)
                renderNode3D(root,camera.transform,cameraModel3DMatrix,renderContext)
            gl.disable(gl.DEPTH_TEST)
            gl.disable(gl.CULL_FACE)
            end()
            outputRenderPassData.values[RenderPassData.RENDER_TARGET_TEXTURE]
        }
        return super.render(renderContext,gl,camera,camera2D,root, dt, outputRenderPassData)
    }

    private fun renderNode3D(node: Node, model: Matrix4fc, projection: Matrix4fc, renderContext: RenderContext) {
        var pos = model
        if (node.isVisualInstance) {
            node as VisualInstance
            if (!node.visible)
                return
            pos = node.matrix
            node.render(node.matrix, projection, renderContext)
        }

        node.childs.forEach {
            renderNode3D(it, pos, projection, renderContext)
        }
    }
}