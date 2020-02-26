package pw.binom.material

object Default2DMaterial {
    const val IMAGE_PROPERTY = "image"
    const val SOURCE = """
@property
sampler2D $IMAGE_PROPERTY

@uv
vec2 vertexUV

@vertex
vec3 vertexPos

@projection
mat4 projection

@model
mat4 model

vec4 vertex(){
    return vec4(projection * model * vec4(vertexPos, 1f))
}

vec4 fragment(vec4 color2){
    vec4 tex = texture($IMAGE_PROPERTY, vertexUV).rgba
    return tex
}
    """
}