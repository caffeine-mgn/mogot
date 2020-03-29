package mogot.gl

class SimplePostEffect(gl: GL, override val shader: Shader) : MaterialGLSL(gl) {

    override fun dispose() {
        shader.close()
        super.dispose()
    }

}