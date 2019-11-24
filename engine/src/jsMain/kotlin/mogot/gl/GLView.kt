package mogot.gl

import mogot.*
import mogot.math.Matrix4f
import mogot.math.Matrix4fc
import mogot.math.Vector4f
import org.khronos.webgl.WebGLRenderingContext

open class GLView : AbstractGLView() {
    open var camera: Camera? = null
    protected open val root
        get() = camera?.asUpSequence()?.last()

    val backgroundColor
        get() = renderContext.sceneColor

    private object renderContext : RenderContext {
        override val pointLights = ArrayList<PointLight>()
        override val sceneColor: Vector4f = Vector4f(0f, 0f, 0f, 1f)
    }

    private val viewMatrix = Matrix4f()

    val engine = Engine(this)

    override fun draw() {
        super.draw()
        gl.ctx.viewport(0, 0, width, height)
        gl.ctx.clear(WebGLRenderingContext.COLOR_BUFFER_BIT or WebGLRenderingContext.DEPTH_BUFFER_BIT)
        gl.ctx.enable(WebGLRenderingContext.DEPTH_TEST)
        gl.ctx.enable(WebGLRenderingContext.CULL_FACE)
        camera?.applyMatrix(viewMatrix.identity())
        renderContext.pointLights.clear()
        root?.walk {
            if (it is PointLight)
                renderContext.pointLights += it
            true
        }
        if (root != null) {
            update(root!!, viewMatrix)
            renderNode3D(root!!, viewMatrix, camera!!.projectionMatrix, renderContext)
        }
    }

    private var width: Int = 0
    private var height: Int = 0
    override fun setup(width: Int, height: Int) {
        this.width = width
        this.height = height
        super.setup(width, height)
        gl.ctx.clearColor(renderContext.sceneColor.x,renderContext.sceneColor.y,renderContext.sceneColor.z,renderContext.sceneColor.w)
        camera?.resize(width, height)
    }

    private fun update(node: Node, model: Matrix4fc) {
        var pos = model
        if (node is Spatial) {
            pos = node.apply(model)
        }

        node.childs.forEach {
            update(it, pos)
        }
    }

    private fun renderNode3D(node: Node, model: Matrix4fc, projection: Matrix4fc, renderContext: RenderContext) {
        var pos = model
        if (node is VisualInstance) {
//            pos = node.apply(model)
            pos = node.matrix
            node.render(node.matrix, projection, renderContext)
        }

        node.childs.forEach {
            renderNode3D(it, pos, projection, renderContext)
        }
    }
}