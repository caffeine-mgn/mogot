package pw.binom

import mogot.Engine
import mogot.gl.MaterialGLSL
import mogot.Texture2D
import mogot.gl.Shader
import mogot.math.Matrix4fc
import mogot.math.Vector4f
import mogot.rendering.Display

internal class SolidTextureMaterial(engine: Engine) : MaterialGLSL(engine) {

    override fun dispose() {
        shader.close()
        super.dispose()
    }

    val diffuseColor = Vector4f(1f, 1f, 1f, 1f)
    override val shader: Shader = Shader(engine.gl,
            vertex = """#version 440 core

layout(location = 0) in vec3 gles_vertex;
layout(location = 2) in vec2 gles_uv;

uniform mat4 gles_projection;
uniform mat4 gles_model;
out mediump vec2 UV;

void main() {
    gl_Position = gles_projection * gles_model * vec4(gles_vertex, 1.0f);
    UV = gles_uv;
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
                gl.gl.activeTexture(gl.gl.TEXTURE0)
                gl.gl.bindTexture(gl.gl.TEXTURE_2D, tex!!.gl)
                shader.uniform("tex", 0)
            } else {
                gl.gl.activeTexture(gl.gl.TEXTURE0)
                gl.gl.bindTexture(gl.gl.TEXTURE_2D, null)
            }
        }

    override fun use(model: Matrix4fc, projection: Matrix4fc, context: Display.Context) {
        super.use(model, projection, context)
        if (tex != null) {
            gl.gl.bindTexture(gl.gl.TEXTURE_2D, tex!!.gl)
        }
        shader.uniform("diffuseColor", diffuseColor.x, diffuseColor.y, diffuseColor.z, diffuseColor.w)
    }
}