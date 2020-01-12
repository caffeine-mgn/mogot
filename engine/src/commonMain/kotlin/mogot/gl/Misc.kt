package mogot.gl

enum class RenderBufferFormat{
    DEPTH_COMPONENT16,
    DEPTH_COMPONENT24,
    DEPTH24_STENCIL8,
    STENCIL_INDEX8,
    GL_RGB8
}

inline fun getRenderBufferFormat(gl: GL, format:RenderBufferFormat): Int {
    return when(format){
        RenderBufferFormat.DEPTH_COMPONENT16 -> TODO()
        RenderBufferFormat.DEPTH_COMPONENT24 -> TODO()
        RenderBufferFormat.DEPTH24_STENCIL8 -> gl.DEPTH24_STENCIL8
        RenderBufferFormat.STENCIL_INDEX8 -> TODO()
        RenderBufferFormat.GL_RGB8 -> TODO()
    }
}