package pw.binom

import com.jogamp.opengl.GL2
import mogot.gl.MaterialGLSL
import mogot.RenderContext
import mogot.Texture2D
import mogot.gl.GL
import mogot.gl.Shader
import mogot.math.Matrix4fc
import mogot.math.Vector4f

internal class SolidTextureMaterial(gl: GL) : MaterialGLSL(gl) {
    override fun close() {
        shader.close()
    }

    val diffuseColor = Vector4f(1f, 1f, 1f, 1f)
    override val shader: Shader = Shader(gl,
            vertex = """#version 440 core

layout(location = 0) in vec3 vertexPos;
layout(location = 2) in vec2 vertexUV;

uniform mat4 projection;
uniform mat4 model;
out mediump vec2 UV;

void main() {
    gl_Position = projection * model * vec4(vertexPos, 1.0);
    UV = vertexUV;
}""",
            fragment = """#version 440 core

uniform vec4 diffuseColor;
out vec4 color;
uniform sampler2D tex;
in lowp vec2 UV;

void main() {
    color = texture(tex, UV).rgba + diffuseColor;
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

    override fun use(model: Matrix4fc, projection: Matrix4fc, renderContext: RenderContext) {
        super.use(model, projection, renderContext)
        if (tex != null) {
            gl.bindTexture(gl.TEXTURE_2D, tex!!.gl)
        }
        shader.uniform("diffuseColor", diffuseColor.x, diffuseColor.y, diffuseColor.z, diffuseColor.w)
    }
}