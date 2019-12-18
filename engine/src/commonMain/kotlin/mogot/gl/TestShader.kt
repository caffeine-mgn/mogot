package mogot.gl

fun getNoColorShader(gl:GL):Shader {
    return Shader(gl,
            vertex =
            """#version 450 core
                layout (location = 0) in vec2 aPos;
                layout (location = 2) in vec2 aTexCoords;
                out vec2 TexCoords;
                void main()
                {
                    gl_Position = vec4(aPos.x, aPos.y, 0.0, 1.0);
                    TexCoords = aTexCoords;
                }""",
            fragment =
            """#version 450 core
void main(void)
{
    gl_FragColor = vec4(1.0,1.0,1.0,1.0);
}""")


}