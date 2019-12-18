package mogot.gl

import mogot.*
import mogot.math.Matrix4fc
import mogot.math.Vector3f

abstract class MaterialGLSL(val engine: Engine): Material, ResourceImpl() {
    abstract val shader: Shader

    fun projection(projection: Matrix4fc){
        shader.uniform("projection", projection)
    }

    fun model(model: Matrix4fc){
        shader.uniform("model", model)
    }

    val TEMP_VECTOR3F = Vector3f()

    override fun use(model: Matrix4fc, projection: Matrix4fc, renderContext: RenderContext) {
        shader.use()
        shader.uniform("projection", projection)
        shader.uniform("model", model)
            shader.uniform("lights_len", renderContext.pointLights.size)
        renderContext.pointLights.forEachIndexed { index, light ->
            light.matrix.getTranslation(TEMP_VECTOR3F)
            shader.uniform("lights[$index].position", TEMP_VECTOR3F)
            shader.uniform("lights[$index].diffuse", light.diffuse)
            shader.uniform("lights[$index].specular", light.specular)
        }
    }

    override fun unuse() {
        engine.gl.useProgram(null)
    }
}