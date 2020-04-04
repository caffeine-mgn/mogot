package mogot.rendering

import mogot.*
import mogot.gl.GL
import mogot.gl.TextureObject
import mogot.gl.checkError
import mogot.math.Vector4f
import mogot.math.isPowerOfTwo
import mogot.math.nextPowerOfTwo

open class Display(private val renderPassChain: RenderPass, private val startRenderPassData: RenderPassData = RenderPassData()) {
    class Context {
        var backgroundColor = Vector4f(0.0f, 0.0f, 0.0f, 1.0f)
        val lights = ArrayList<Light>()
        var camera: Camera? = null
        var camera2D: Camera2D? = null
        var width: Int = 800
        var height: Int = 800
        var x: Int = 0
        var y: Int = 0
    }

    val context: Context = Context()
    private var lastFrameTime = CurrentTime.getNano()
    private var time = CurrentTime.getNano()
    var deltaTime: Float = Float.MAX_VALUE


    fun setup(gl: GL, x: Int, y: Int, width: Int, height: Int) {
        if(width!=0)
            context.width = width
        if(height!=0)
            context.height = height
        context.x = x
        context.y = y
        gl.checkError{""}
        gl.viewport(x, y, width, height)
        gl.checkError{""}
        gl.clearColor(context.backgroundColor.x, context.backgroundColor.y, context.backgroundColor.z, context.backgroundColor.w)
        gl.checkError{""}
        gl.blendFunc(gl.SRC_ALPHA, gl.ONE_MINUS_SRC_ALPHA)
        gl.checkError{""}
        gl.enable(gl.BLEND)
        gl.checkError{""}
        gl.disable(gl.MULTISAMPLE)
        gl.checkError{""}
        context.camera?.resize(width,height)
        context.camera2D?.resize(width,height)
        renderPassChain.setup(context,gl,width,height,TextureObject.MSAALevels.Disable)
    }

    protected open fun process(root: Node){
        context.lights.clear()
        root.walk {
            if (it is Light){
                context.lights += it
            }
            else if (it is Camera) {
                if (it.enabled) {
                    if(context.camera!=it) {
                        context.camera?.enabled = false
                        context.camera = it
                        context.camera?.resize(context.width, context.height)
                    }
                }
            }else if(it is Camera2D){
                if (it.enabled) {
                    if(context.camera2D!=it) {
                        context.camera2D?.enabled = false
                        context.camera2D = it
                        context.camera2D?.resize(context.width, context.height)
                    }
                }
            }
            true
        }
    }


    fun render(gl: GL, root: Node) {
        process(root)
        time = CurrentTime.getNano()
        deltaTime = (time - lastFrameTime) / 1e+9f
        gl.checkError{""}
        renderPassChain.render(context, gl, root, deltaTime, startRenderPassData)
        gl.checkError{""}
        lastFrameTime = time
    }


}