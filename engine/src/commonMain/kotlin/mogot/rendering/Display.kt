package mogot.rendering

import mogot.*
import mogot.gl.GL

open class Display(private val renderPassChain: RenderPass, private val startRenderPassData: RenderPassData = RenderPassData()) {
    private var lastFrameTime = CurrentTime.getNano()
    private var time = CurrentTime.getNano()
    var deltaTime: Float = Float.MAX_VALUE

    fun setup(renderContext: RenderContext,gl: GL,width: Int, height: Int) {
        gl.viewport(0, 0, width, height)
        startRenderPassData.values["width"] = width
        startRenderPassData.values["height"] = height
        gl.clearColor(renderContext.sceneColor.x, renderContext.sceneColor.y, renderContext.sceneColor.z, renderContext.sceneColor.w)
        gl.blendFunc(gl.SRC_ALPHA, gl.ONE_MINUS_SRC_ALPHA);
        gl.enable(gl.BLEND)
        gl.disable(gl.MULTISAMPLE)
    }


    fun render(renderContext: RenderContext,gl: GL, camera: Camera?, camera2D: Camera2D?, root: Node) {
        time = CurrentTime.getNano()
        deltaTime = (time - lastFrameTime) / 1e+9f

        renderPassChain.render(renderContext,gl, camera, camera2D, root, deltaTime, startRenderPassData)

        lastFrameTime = time
    }


}