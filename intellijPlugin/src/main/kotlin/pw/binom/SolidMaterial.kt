package pw.binom

import mogot.gl.GL
import mogot.gl.MaterialGLSL
import mogot.gl.Shader
import mogot.math.Matrix4fc
import mogot.math.Vector4f
import mogot.rendering.Display

internal class SolidMaterial(gl: GL) : MaterialGLSL(gl) {

    override fun dispose() {
        shader.close()
        super.dispose()
    }

    val diffuseColor = Vector4f(1f, 1f, 1f, 1f)
    override val shader: Shader = Shader(gl,
            vertex = """#version 440 core

layout(location = 0) in vec3 vertexPos;

uniform mat4 $PROJECTION;
uniform mat4 $MODEL_VIEW;

void main() {
    gl_Position = $PROJECTION * $MODEL_VIEW * vec4(vertexPos, 1.0);
}""",
            fragment = """#version 440 core

uniform vec4 diffuseColor;
out vec4 color;

void main() {
    color = diffuseColor;
}
"""
    )

    override fun use(model: Matrix4fc, modelView: Matrix4fc, projection: Matrix4fc, context: Display.Context) {
        super.use(model, modelView, projection, context)
        shader.uniform("diffuseColor", diffuseColor.x, diffuseColor.y, diffuseColor.z, diffuseColor.w)
    }
}