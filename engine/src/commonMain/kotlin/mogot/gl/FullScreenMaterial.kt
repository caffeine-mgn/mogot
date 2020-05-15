package mogot.gl

import mogot.math.Matrix4fc
import mogot.rendering.Display

class FullScreenMaterial(gl: GL) : MaterialGLSL(gl) {
    override val shader: Shader
        get() = Shader(gl, """#version 450 core
                                        layout (location = 0) in vec3 aPos;
                                        layout (location = 2) in vec2 aTexCoords;
                                        out vec2 TexCoords;
                                        
                                        void main()
                                        {
                                            gl_Position = vec4(aPos.x, aPos.y, 0.0, 1.0);
                                            TexCoords = aTexCoords;
                                        }""",
                """#version 450 core
                                out vec4 FragColor;
                                in vec2 TexCoords;
                                uniform sampler2D screenTexture;
                                void main()
                                { 
                                    FragColor = texture(screenTexture, TexCoords);
                                }""")

    override fun dispose() {
        shader.close()
        super.dispose()
    }

    var texture2D: GLTexture? = null

    override fun use(model: Matrix4fc, modelView:Matrix4fc, projection: Matrix4fc, context: Display.Context) {
        super.use(model, modelView, projection, context)
        if (texture2D != null) {
            gl.activeTexture(gl.TEXTURE0)
            gl.bindTexture(gl.TEXTURE_2D, texture2D!!)
            shader.use()
            shader.uniform("screenTexture", 0)

        }
    }

    override fun unuse() {
        if (texture2D != null) {
            gl.activeTexture(gl.TEXTURE0)
            gl.bindTexture(gl.TEXTURE_2D, null)
        }
        super.unuse()
    }
}