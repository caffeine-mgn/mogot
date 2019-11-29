package mogot.gl

import mogot.Engine
import mogot.RenderContext
import mogot.math.Matrix4fc

class SimplePostEffect(engine: Engine, override val shader: Shader) : MaterialGLSL(engine.gl) {

    override fun close() {
        shader.close()
    }


    override fun use(model: Matrix4fc, projection: Matrix4fc, renderContext: RenderContext) {
        super.use(model, projection, renderContext)
    }

    override fun unuse() {
        super.unuse()
    }
}