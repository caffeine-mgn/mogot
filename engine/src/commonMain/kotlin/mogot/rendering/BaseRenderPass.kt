package mogot.rendering

import mogot.Camera
import mogot.Camera2D
import mogot.Node
import mogot.gl.GL

abstract class BaseRenderPass(override var next: RenderPass?) : RenderPass {
    protected val outputRenderPassData = RenderPassData()
    var bypass = false
    override fun render(context: Display.Context, gl: GL, camera: Camera?, camera2D: Camera2D?, root: Node, dt:Float, inputRenderPassData: RenderPassData): RenderPassData {
        return next?.render(context,gl, camera, camera2D,root,dt,outputRenderPassData)?:outputRenderPassData
    }

    override fun cleanup() {
        next?.cleanup()
    }
}