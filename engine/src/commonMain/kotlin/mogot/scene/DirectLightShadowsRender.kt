package mogot.scene

import mogot.*
import mogot.gl.DepthShader
import mogot.gl.FrameBuffer
import mogot.gl.GL
import mogot.gl.TextureObject
import mogot.math.*
import pw.binom.io.Closeable

open class ShadowRender(): Closeable{
    override fun close() {

    }
}
class DirectLightShadowsRender(val gl: GL, val shadowWidth:Int, val shadowHeight:Int, val screenWidth: Int, val screenHeight: Int, val nearPlane: Float = 1.0f, val farPlane:Float = 7.5f, var maxLights:Int = 1): ShadowRender() {
    private var depthBuffer: FrameBuffer? = FrameBuffer(
            gl = gl,
            texture = TextureObject(gl, shadowWidth, shadowHeight, TextureObject.MinFilterParameter.Nearest, TextureObject.MagFilterParameter.Nearest, TextureObject.TextureWrap.Repeat, TextureObject.TextureWrap.Repeat, TextureObject.MSAALevels.Disable, TextureObject.Format.DEPTH_COMPONENT))

    private var depthShader: DepthShader? = DepthShader(gl)
    override fun close() {
        depthBuffer?.close()
        depthBuffer = null
        depthShader?.close()
        depthShader = null
        super.close()
    }

    val shadowMapsBuffer = mutableListOf<Texture2D>().apply {
        for(i in 1..maxLights){
            add(Texture2D(TextureObject(gl, shadowWidth, shadowHeight, depthBuffer?.texture?.minFilter!!, depthBuffer?.texture?.magFilter!!, depthBuffer?.texture?.textureWrapS!!,depthBuffer?.texture?.textureWrapT!!,TextureObject.MSAALevels.Disable,TextureObject.Format.DEPTH_COMPONENT)))
        }
    }

    private val renderResult = mutableListOf<Texture2D>()
    fun render(cameraPosition: Vector3fm,root: Node, renderContext: RenderContext){
        val directsLight = renderContext.lights.filter { it.isDirectLight }
        val sorted = directsLight.sortedBy { (it.position-cameraPosition).lengthSquared }
        renderResult.clear()
        for(i in 0 until (if(directsLight.size>maxLights) maxLights else directsLight.size)){
            renderResult.add(renderDepth(sorted[i] as DirectLight,root,renderContext,i))
        }
        renderContext.shadowMaps.clear()
        renderContext.shadowMaps.addAll(renderResult)
    }

    private fun renderDepth(light: DirectLight, root: Node, renderContext: RenderContext, lightIndex:Int):Texture2D{
        requireNotNull(depthBuffer)
        requireNotNull(depthShader)
        val lightProjection = Matrix4f().identity().ortho3D(-10f,10f,-10f,10f,nearPlane,farPlane)
        val lightView = Matrix4f().identity().translate(light.position).rotate(Quaternionf().identity().lookAt(light.direction, Vector3fc.UP))

        gl.viewPort(0,0,shadowWidth,screenHeight)
        depthBuffer?.bind()
        gl.clear(gl.DEPTH_BUFFER_BIT)
        renderNode3DShadowDepth(root,light.matrix,lightView,lightProjection,renderContext,depthShader!!)
        depthBuffer?.unbind()
        copyToTexture(shadowMapsBuffer[lightIndex].textureObject)
        return shadowMapsBuffer[lightIndex]
    }

    private fun copyToTexture(textureObject: TextureObject){
        gl.bindTexture(gl.TEXTURE_2D,textureObject.glTexture)
        depthBuffer?.bind(read = true, draw = false)
        gl.copyTexSubImage2D(gl.TEXTURE_2D,0,0,0,0,0,shadowWidth,shadowHeight)
        gl.bindTexture(gl.TEXTURE_2D,null)
        depthBuffer?.unbind(read = true, draw = false)
    }


    private fun renderNode3DShadowDepth(node: Node, model: Matrix4fc, view: Matrix4fc, projection: Matrix4fc, renderContext: RenderContext, shader: DepthShader) {
        var pos = model
        if (node.isVisualInstance) {
            node as VisualInstance
            if (!node.visible)
                return
            if(!node.shadow)
                return
            pos = node.matrix
            node.renderToShadowMap(node.matrix,view, projection, renderContext, shader)
        }

        node.childs.forEach {
            renderNode3DShadowDepth(it, pos,view, projection, renderContext, shader)
        }
    }

}