package mogot.rendering

import mogot.Camera
import mogot.Camera2D
import mogot.Node
import mogot.gl.GL
import mogot.gl.TextureObject


interface RenderPass {
    var next: RenderPass?
    fun render(renderContext: Display.Context, gl: GL, root: Node, dt: Float, inputRenderPassData: RenderPassData): RenderPassData
    fun cleanup()
    fun setup(renderContext: Display.Context, gl: GL, msaaLevel: TextureObject.MSAALevels)
}