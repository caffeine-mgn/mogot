package pw.binom.sceneEditor

import mogot.RenderContext
import mogot.Texture2D
import mogot.gl.GL
import mogot.gl.MaterialGLSL
import mogot.gl.Shader
import mogot.math.Matrix4fc
import mogot.math.Vector4f

internal class SimpleMaterial(gl: GL) : MaterialGLSL(gl) {
    override fun close() {
        shader.close()
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

uniform mat4 projection;
uniform mat4 model;

//uniform mat4 gl_ModelViewMatrix;
//uniform mat4 gl_ProjectionMatrix;
//uniform mat3 gl_NormalMatrix;
out mediump vec2 UV;
out mediump vec3 normal;
out mediump vec3 vVertex;

void main() {
    mat3 normalMatrix = mat3(transpose(inverse(model)));
    normal = vec3(normalMatrix * normalList);
    gl_Position = projection * model * vec4(vertexPos, 1.0);
    //norm=normalize(vec3(gl_NormalMatrix * normalList));
    vVertex = vec3(model * vec4(vertexPos, 1.0));
    UV = vertexUV;
}""",
            fragment = """#version 300 es

#ifdef GL_ES
precision mediump float;
#endif

uniform sampler2D tex;
uniform lowp vec4 diffuseColor;
in lowp vec2 UV;
in vec3 normal;
out vec4 color;
uniform mat4 projection;
uniform mat4 model;
uniform mat4 model_projection;
in vec3 vVertex;

struct Light {
    lowp vec3 position;
    lowp vec3 diffuse;
    lowp float specular;
};
uniform Light lights[10];
uniform highp int lights_len;
uniform lowp vec4 FrontMaterial_specular;
uniform lowp float FrontMaterial_shininess;

void main() {
//    color = texture(tex, UV).rgba + diffuseColor;
//    color = vec4(texture(tex, UV).rgb,0.5f);
//    color = texture(tex, UV).rgba * diffuseColor;
//    vec4 cc = texture(tex, UV).rgba * diffuseColor;
//    lowp vec4 cc = vec4(0.5,0.5,0.5,1);
    mediump vec4 cc = texture(tex, UV).rgba;// * diffuseColor;
    color = cc;
    return;
      
    for (int i = 0; i<lights_len; i++){
        lowp vec3 lightDir = vec3(lights[i].position.xyz - vVertex);
        lowp vec3 N = normalize(normal);
        lowp vec3 L = normalize(lightDir);

        lowp float lambertTerm = dot(N,L);
        lowp float cosTheta = dot( N,L );
        lowp float distation = length(lightDir);

//        if(lambertTerm > 0.0){

            lowp vec3 E = normalize(-vVertex);
            lowp vec3 R = normalize(-reflect(L, N));
            lowp float LightPower = 1000.0f;

            //calculate Diffuse Term:
            cc += vec4(lights[i].diffuse, 1.0f) * LightPower * cosTheta / (distation*distation);


            //calculate Specular Term
//          float specular = pow( max(dot(R, E), 0.0), FrontMaterial_shininess );
//          cc += lights[i].specular * FrontMaterial_specular * specular;

            lowp vec4 Ispec = FrontMaterial_specular * pow( max(dot(R,E),0.0), 0.3*FrontMaterial_shininess);
            Ispec = clamp(Ispec, 0.0, 1.0);
            cc += Ispec;
//        }
    }
    color = cc;
    //color=vec4(normalize(normal),1);
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