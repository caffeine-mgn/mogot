package mogot.rendering

import mogot.*
import mogot.gl.GL
import mogot.gl.TextureObject
import mogot.gl.checkError
import mogot.math.Matrix4f
import mogot.math.Matrix4fc
import mogot.math.Vector4f

open class Display(private val renderPassChain: List<RenderPass>, private val startRenderPassData: RenderPassData = RenderPassData()) {
    class Context {
        var backgroundColor = Vector4f(0.0f, 0.0f, 0.0f, 1.0f)
        val lights = ArrayList<Light>()
        var camera: Camera? = null
        var camera2D: Camera2D? = null
        var width: Int = 800
        var height: Int = 800
        var x: Int = 0
        var y: Int = 0
        var deltaTime: Float = Float.MAX_VALUE
        private val cameraModel3DMatrix = Matrix4f()
        private val cameraModel2DMatrix = Matrix4f()
        fun calcModels() {
            camera?.globalToLocalMatrix(cameraModel3DMatrix.identity())
            camera2D?.globalToLocalMatrix(cameraModel2DMatrix.identity())
        }

        private val TMP_MODEL_MATRIX = Matrix4f()
        private val TMP_MODEL_VIEW_MATRIX = Matrix4f()
        private val TMP_VIEW_MATRIX = Matrix4f()

        fun update(node: Node) {
            node.update(deltaTime)
            var mat3d = cameraModel3DMatrix as Matrix4fc
            var mat2d = cameraModel2DMatrix as Matrix4fc

            if (node.isSpatial())
                mat3d = node.apply(mat3d)

            if (node.isSpatial2D())
                mat2d = node.apply(mat2d)


            node.childs.forEach {
                update(it)
            }
        }

        fun renderNode3D(node: Node) {
            camera!!.globalToLocalMatrix(TMP_VIEW_MATRIX)
            renderNode3D(node, TMP_VIEW_MATRIX, camera!!.projectionMatrix, this)
        }

        fun renderNode3D(node: Node, view: Matrix4fc, projection: Matrix4fc, context: Display.Context) {
            //var pos = model
            if (node.isVisualInstance) {
                node as VisualInstance
                if (!node.visible)
                    return
                node.localToGlobalMatrix(TMP_MODEL_MATRIX)
                view.mul(TMP_MODEL_MATRIX, TMP_MODEL_VIEW_MATRIX)
                node.render(TMP_MODEL_MATRIX, TMP_MODEL_VIEW_MATRIX, projection, context)
            }

            node.childs.forEach {
                renderNode3D(it, view, projection, context)
            }
        }

        fun renderNode2D(node: Node, projection: Matrix4fc, context: Display.Context) {
            if (node.isVisualInstance2D()) {
                if (!node.visible)
                    return
                node.render(node.matrix, node.matrix, projection, context)
            }
            node.childs.forEach {
                renderNode2D(it, projection, context)
            }
        }
    }

    val context: Context = Context()
    private var lastFrameTime = CurrentTime.getNano()
    private var time = CurrentTime.getNano()


    fun setup(gl: GL, x: Int, y: Int, width: Int, height: Int) {
        if (width != 0)
            context.width = width
        if (height != 0)
            context.height = height
        context.x = x
        context.y = y
        gl.checkError { "" }
        gl.viewport(x, y, width, height)
        gl.checkError { "" }
        gl.clearColor(context.backgroundColor.x, context.backgroundColor.y, context.backgroundColor.z, context.backgroundColor.w)
        gl.checkError { "" }
        gl.blendFunc(gl.SRC_ALPHA, gl.ONE_MINUS_SRC_ALPHA)
        gl.checkError { "" }
        gl.enable(gl.BLEND)
        gl.checkError { "" }
        gl.disable(gl.MULTISAMPLE)
        gl.checkError { "" }
        context.camera?.resize(width, height)
        context.camera2D?.resize(width, height)

        renderPassChain.forEach {
            it.setup(context, gl, TextureObject.MSAALevels.Disable)
        }
    }

    protected open fun process(root: Node) {
        context.lights.clear()
        root.walk {
            if (it is Light) {
                context.lights += it
            } else if (it is Camera) {
                if (it.enabled) {
                    if (context.camera != it) {
                        context.camera?.enabled = false
                        context.camera = it
                        context.camera?.resize(context.width, context.height)
                    }
                }
            } else if (it is Camera2D) {
                if (it.enabled) {
                    if (context.camera2D != it) {
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
        context.calcModels()
        context.update(root)
        time = CurrentTime.getNano()
        context.deltaTime = (time - lastFrameTime) / 1e+9f
        gl.checkError { "" }
        var data = startRenderPassData
        renderPassChain.forEach {
            data = it.render(context, gl, root, context.deltaTime, data)
        }
        gl.checkError { "" }
        lastFrameTime = time
    }


}