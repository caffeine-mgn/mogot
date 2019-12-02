package pw.binom

import com.jogamp.opengl.GL2
import mogot.gl.MaterialGLSL
import mogot.RenderContext
import mogot.gl.GL
import mogot.gl.Shader
import mogot.math.Matrix4fc
import mogot.math.Vector4f

internal class SolidMaterial(gl: GL) : MaterialGLSL(gl) {
    override fun close() {
        shader.close()
    }

    val diffuseColor = Vector4f(1f, 1f, 1f, 1f)
    override val shader: Shader = Shader(gl,
            vertex = """#version 440 core

layout(location = 0) in vec3 vertexPos;

uniform mat4 projection;
uniform mat4 model;

void main() {
    gl_Position = projection * model * vec4(vertexPos, 1.0);
}""",
            fragment = """#version 440 core

uniform vec4 diffuseColor;
out vec4 color;

void main() {
    color = diffuseColor;
}
"""
    )

    override fun use(model: Matrix4fc, projection: Matrix4fc, renderContext: RenderContext) {
        super.use(model, projection, renderContext)
        shader.uniform("diffuseColor", diffuseColor.x, diffuseColor.y, diffuseColor.z, diffuseColor.w)
    }
}