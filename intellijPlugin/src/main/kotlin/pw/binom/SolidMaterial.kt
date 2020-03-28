package pw.binom

import mogot.Engine
import mogot.gl.MaterialGLSL
import mogot.gl.Shader
import mogot.math.Matrix4fc
import mogot.math.Vector4f
import mogot.rendering.Display

internal class SolidMaterial(engine: Engine) : MaterialGLSL(engine) {

    override fun dispose() {
        shader.close()
        super.dispose()
    }

    val diffuseColor = Vector4f(1f, 1f, 1f, 1f)
    override val shader: Shader = Shader(engine.gl,
            vertex = """#version 440 core

layout(location = 0) in vec3 vertexPos;

uniform mat4 gles_projection;
uniform mat4 gles_model;

void main() {
    gl_Position = gles_projection * gles_model * vec4(vertexPos, 1.0);
}""",
            fragment = """#version 440 core

uniform vec4 diffuseColor;
out vec4 color;

void main() {
    color = diffuseColor;
}
"""
    )

    override fun use(model: Matrix4fc, projection: Matrix4fc, context: Display.Context) {
        super.use(model, projection, context)
        shader.uniform("diffuseColor", diffuseColor.x, diffuseColor.y, diffuseColor.z, diffuseColor.w)
    }
}