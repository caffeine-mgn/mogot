package mogot.rendering

import mogot.Camera
import mogot.Camera2D
import mogot.Node
import mogot.RenderContext
import mogot.gl.GL

abstract class BaseRenderPass : RenderPass {
    override var next: RenderPass? = null
    protected val outputRenderPassData = RenderPassData()

    override fun render(renderContext: RenderContext, gl: GL, camera: Camera?, camera2D: Camera2D?, root: Node, dt:Float, inputRenderPassData: RenderPassData): RenderPassData {
        return next?.render(renderContext,gl, camera, camera2D,root,dt,outputRenderPassData)?:outputRenderPassData
    }
}