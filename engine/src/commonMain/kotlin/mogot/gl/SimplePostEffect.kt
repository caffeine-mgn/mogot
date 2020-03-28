package mogot.gl

import mogot.Engine
import mogot.math.Matrix4fc
import mogot.rendering.Display

class SimplePostEffect(engine: Engine, override val shader: Shader) : MaterialGLSL(engine) {

    override fun dispose() {
        shader.close()
        super.dispose()
    }

}