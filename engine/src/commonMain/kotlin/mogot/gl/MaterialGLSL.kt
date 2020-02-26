package mogot.gl

import mogot.*
import mogot.math.Matrix4fc
import mogot.math.Vector3f

abstract class MaterialGLSL(val engine: Engine) : Material, ResourceImpl() {
    override var reservedTexturesMaxId: Int = 0
    companion object{
        const val PROJECTION="gles_projection"
        const val MODEL="gles_model"
    }
    abstract val shader: Shader
    protected var closed = false

    fun projection(projection: Matrix4fc) {
        shader.uniform(PROJECTION, projection)
    }

    fun model(model: Matrix4fc) {
        shader.uniform(MODEL, model)
    }

    val TEMP_VECTOR3F = Vector3f()

    override fun use(model: Matrix4fc, projection: Matrix4fc, renderContext: RenderContext) {
        check(!closed){"Material closed"}
        engine.gl.checkError { "Before set material properties" }
        shader.use()
        engine.gl.checkError { "1" }
        shader.uniform(PROJECTION, projection)
        engine.gl.checkError { "2" }
        shader.uniform(MODEL, model)
        engine.gl.checkError { "2" }
        shader.uniform("lights_len", renderContext.lights.size)
        engine.gl.checkError { "3" }
        renderContext.lights.forEachIndexed { index, light ->
            light.matrix.getTranslation(TEMP_VECTOR3F)
            shader.uniform("lights[$index].position", TEMP_VECTOR3F)
            shader.uniform("lights[$index].diffuse", light.diffuse)
            shader.uniform("lights[$index].specular", light.specular)
        }
        renderContext.shadowMaps.forEachIndexed { index, texture2D ->
            engine.gl.enable(engine.gl.TEXTURE0+index)
            engine.gl.bindTexture(engine.gl.TEXTURE_2D,texture2D.glTexture)
            shader.uniform("shadowMaps[$index]",index)
        }
        reservedTexturesMaxId = renderContext.shadowMaps.size - 1
        engine.gl.checkError { "After set material properties" }
    }

    override fun dispose() {
        check(!closed){"Material already closed"}
        closed = true
        super.dispose()
    }

    override fun unuse() {
        engine.gl.useProgram(null)
    }
}