package mogot.rendering

import mogot.Camera
import mogot.Camera2D
import mogot.Node
import mogot.RenderContext
import mogot.gl.GL


interface RenderPass {
    var next: RenderPass?
    fun render(renderContext: RenderContext, gl: GL, camera: Camera?, camera2D: Camera2D?, root: Node, dt: Float, inputRenderPassData: RenderPassData): RenderPassData
}