package pw.binom

import com.jogamp.opengl.GL2
import mogot.Engine
import mogot.gl.MaterialGLSL
import mogot.RenderContext
import mogot.gl.GL
import mogot.gl.Shader
import mogot.math.Matrix4fc
import mogot.math.Vector4f

internal class SimpleMaterial(engile:Engine) : MaterialGLSL(engile) {

    override fun dispose() {
        shader.close()
        super.dispose()
    }

    //    var image: Image? = null

    val diffuseColor = Vector4f(1f, 1f, 1f, 1f)
    override val shader: Shader = Shader(engile.gl,
            vertex = """#version 440 core

layout(location = 0) in vec3 vertexPos;
layout(location = 1) in vec3 normalList;
layout(location = 2) in vec2 vertexUV;

uniform mat4 projection;
uniform mat4 model;

uniform mat4 gl_ModelViewMatrix;
uniform mat4 gl_ProjectionMatrix;
uniform mat3 gl_NormalMatrix;
out vec2 UV;
out vec3 normal;
out vec3 vVertex;

void main() {
    mat3 normalMatrix = mat3(transpose(inverse(model)));
    normal = vec3(normalMatrix * normalList);
    gl_Position = projection * model * vec4(vertexPos, 1.0);
    //norm=normalize(vec3(gl_NormalMatrix * normalList));
    vVertex = vec3(model * vec4(vertexPos, 1.0));
    UV = vertexUV;
}""",
            fragment = """#version 440 core

uniform sampler2D tex;
uniform vec4 diffuseColor;
in vec2 UV;
in vec3 normal;
out vec4 color;
uniform mat4 projection;
uniform mat4 model;
uniform mat4 model_projection;
in vec3 vVertex;

struct Light {
    vec3 position;
    vec3 diffuse;
    float specular;
};
uniform Light lights[10];
uniform int lights_len;
uniform vec4 FrontMaterial_specular;
uniform float FrontMaterial_shininess;

void main() {
//    color = texture(tex, UV).rgba + diffuseColor;
//    color = vec4(texture(tex, UV).rgb,0.5f);
//    color = texture(tex, UV).rgba * diffuseColor;
//    vec4 cc = texture(tex, UV).rgba * diffuseColor;
    vec4 cc = vec4(0.5,0.5,0.5,1);
      
    for (int i = 0; i<lights_len; i++){
        vec3 lightDir = vec3(lights[i].position.xyz - vVertex);
        vec3 N = normalize(normal);
        vec3 L = normalize(lightDir);

        float lambertTerm = dot(N,L);
        float cosTheta = dot( N,L );
        float distation = length(lightDir);

//        if(lambertTerm > 0.0){

            vec3 E = normalize(-vVertex);
            vec3 R = normalize(-reflect(L, N));
            float LightPower = 1000;

            //calculate Diffuse Term:
            cc += vec4(lights[i].diffuse, 1f) * LightPower * cosTheta / (distation*distation);


            //calculate Specular Term
//          float specular = pow( max(dot(R, E), 0.0), FrontMaterial_shininess );
//          cc += lights[i].specular * FrontMaterial_specular * specular;

            vec4 Ispec = FrontMaterial_specular * pow( max(dot(R,E),0.0), 0.3*FrontMaterial_shininess);
            Ispec = clamp(Ispec, 0.0, 1.0);
            cc += Ispec;
//        }
    }
    color = cc;
    //color=vec4(normalize(normal),1);
}
"""
    )

    override fun use(model: Matrix4fc, projection: Matrix4fc, renderContext: RenderContext) {
        super.use(model, projection, renderContext)
//        image?.bind()
        shader.uniform("diffuseColor", diffuseColor.x, diffuseColor.y, diffuseColor.z, diffuseColor.w)
    }
}