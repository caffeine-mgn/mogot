package mogot.gl

class DepthShader(gl: GL) : Shader(gl,
        """
        #version 330 es
        layout (location = 0) in vec3 aPos;

        uniform mat4 projection;
        uniform mat4 view;
        uniform mat4 model;

        void main()
        {
            gl_Position = projection * view * model * vec4(aPos, 1.0);
        }
        """.trimIndent(),
        """
        #version 330 es
        void main()
        {
        // gl_FragDepth = gl_FragCoord.z;
        }
        """.trimIndent()) {

}