package mogot.rendering

import mogot.*
import mogot.gl.GL

open class Display(private val renderPassChain: RenderPass, private val startRenderPassData: RenderPassData = RenderPassData()) {
    private var lastFrameTime = PlatformUtils.getSystemTimeNano()
    var time = PlatformUtils.getSystemTimeNano()
    val dt: Float = Float.MAX_VALUE

    fun setup(width: Int, height: Int) {
        startRenderPassData.values["width"] = width
        startRenderPassData.values["height"] = height
    }


    fun render(renderContext: RenderContext,gl: GL, camera: Camera, camera2D: Camera2D, root: Node) {
        time = PlatformUtils.getSystemTimeNano()
        val dt = (time - lastFrameTime) / 1e+9f

        renderPassChain.render(renderContext,gl, camera, camera2D, root, dt, startRenderPassData)

        lastFrameTime = time
    }


}