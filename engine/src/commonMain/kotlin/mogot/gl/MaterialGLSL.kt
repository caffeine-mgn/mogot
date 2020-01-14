package mogot.gl

import mogot.*
import mogot.math.Matrix4fc
import mogot.math.Vector3f

abstract class MaterialGLSL(val engine: Engine) : Material, ResourceImpl() {
    companion object{
        const val PROJECTION="gles_projection"
        const val MODEL="gles_model"
    }
    abstract val shader: Shader
    protected var closed = false

    fun projection(projection: Matrix4fc) {
        shader.uniform("projection", projection)
    }

    fun model(model: Matrix4fc) {
        shader.uniform("model", model)
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
        shader.uniform("lights_len", renderContext.pointLights.size)
        engine.gl.checkError { "3" }
        renderContext.pointLights.forEachIndexed { index, light ->
            light.matrix.getTranslation(TEMP_VECTOR3F)
            shader.uniform("lights[$index].position", TEMP_VECTOR3F)
            shader.uniform("lights[$index].diffuse", light.diffuse)
            shader.uniform("lights[$index].specular", light.specular)
        }
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