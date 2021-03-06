package pw.binom.sceneEditor

import mogot.Texture2D
import mogot.gl.GL
import mogot.gl.MaterialGLSL
import mogot.gl.Shader
import mogot.math.*
import mogot.rendering.Display

internal class SimpleMaterial(gl: GL) : MaterialGLSL(gl) {
    override fun dispose() {
        shader.close()
        super.dispose()
    }

    //    var image: Image? = null
    val diffuseColor = Vector4fProperty(1f, 1f, 1f, 1f)
    override val shader: Shader = Shader(gl,
            vertex = """#version 300 es

#ifdef GL_ES
precision mediump float;
#endif

layout(location = 0) in vec3 vertexPos;
layout(location = 1) in vec3 normalList;
layout(location = 2) in vec2 vertexUV;

uniform mat4 gles_projection;
uniform mat4 gles_model;

void main() {
    gl_Position = vec4(gles_projection * gles_model * vec4(vertexPos, 1.0f));
}""",
            fragment = """#version 300 es

#ifdef GL_ES
precision mediump float;
#endif

out vec4 color;

void main() {
    color = vec4(1.0f, 1.0f, 1.0f, 1.0f);
}
"""
    )

    var tex: Texture2D? = null
        set(value) {
            field = value
            shader.use()
            if (tex != null) {
                gl.activeTexture(gl.TEXTURE0)
                gl.bindTexture(gl.TEXTURE_2D, tex!!.gl)
                shader.uniform("tex", 0)
            } else {
                gl.activeTexture(gl.TEXTURE0)
                gl.bindTexture(gl.TEXTURE_2D, null)
            }
        }

    override fun use(model: Matrix4fc, modelView: Matrix4fc, projection: Matrix4fc, context: Display.Context) {
        super.use(model, modelView, projection, context)
        if (tex != null) {
            gl.bindTexture(gl.TEXTURE_2D, tex!!.gl)
        }
        if (diffuseColor.resetChangeFlag()) {
            shader.uniform("diffuseColor", diffuseColor.x, diffuseColor.y, diffuseColor.z, diffuseColor.w)
        }
    }

    override fun unuse() {
//        if (tex != null)
        gl.bindTexture(gl.TEXTURE_2D, null)
        super.unuse()
    }
}