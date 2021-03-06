package pw.binom

import mogot.gl.GL
import mogot.gl.MaterialGLSL
import mogot.gl.Shader
import mogot.math.Matrix4fc
import mogot.math.Vector4f
import mogot.rendering.Display
import pw.binom.material.EmptyModuleResolver
import pw.binom.material.SourceModule
import pw.binom.material.compiler.Compiler
import pw.binom.material.generator.gles300.GLES300Generator
import pw.binom.material.lex.Parser
import java.io.StringReader

private val shaderText = """
@vertex
vec3 vertexPos

@normal
vec3 normalList

@uv
vec2 vertexUV

@projection
mat4 projection

@modelView
mat4 modelView
vec3 normal


vec4 vertex(){
    return vec4(projection * modelView * vec4(vertexPos, 1f))
}

vec4 fragment(vec4 color2){
    return vec4(0.7f,0.7f,0.7f,1f)
}
 
"""

internal class SimpleMaterial(gl: GL) : MaterialGLSL(gl) {

    override fun dispose() {
        shader.close()
        super.dispose()
    }


    val diffuseColor = Vector4f(1f, 1f, 1f, 1f)
    override val shader: Shader = run {
        val module = SourceModule("")
        val gen = Parser(module, StringReader(shaderText))
                .let { Compiler(it, module, EmptyModuleResolver) }
                .let { GLES300Generator.mix(listOf(module)) }
        Shader(gl, gen.vp, gen.fp)
    }

    /*
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
*/
    override fun use(model: Matrix4fc, modelView: Matrix4fc, projection: Matrix4fc, context: Display.Context) {
        super.use(model, modelView, projection, context)
//        image?.bind()
        shader.uniform("diffuseColor", diffuseColor.x, diffuseColor.y, diffuseColor.z, diffuseColor.w)
    }
}