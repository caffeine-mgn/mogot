package mogot.gl

import mogot.*
import mogot.math.Matrix4fc
import mogot.math.Vector3f
import mogot.rendering.Display

abstract class MaterialGLSL(val gl: GL) : Material, ResourceImpl() {
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

    override fun use(model: Matrix4fc, projection: Matrix4fc, context: Display.Context) {
        check(!closed){"Material closed"}
        gl.checkError { "Before set material properties" }
        shader.use()
        gl.checkError { "1" }
        shader.uniform(PROJECTION, projection)
        gl.checkError { "2" }
        shader.uniform(MODEL, model)
        gl.checkError { "2" }
        shader.uniform("lights_len", context.lights.size)
        gl.checkError { "3" }
        context.lights.forEachIndexed { index, light ->
            light.matrix.getTranslation(TEMP_VECTOR3F)
            shader.uniform("lights[$index].position", TEMP_VECTOR3F)
            shader.uniform("lights[$index].diffuse", light.diffuse)
            shader.uniform("lights[$index].specular", light.specular)
        }
        gl.checkError { "After set material properties" }
    }

    override fun dispose() {
        check(!closed){"Material already closed"}
        closed = true
        super.dispose()
    }

    override fun unuse() {
        shader.unuse()
    }
}