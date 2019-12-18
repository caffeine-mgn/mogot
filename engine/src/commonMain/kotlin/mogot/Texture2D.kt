package mogot

import mogot.gl.GLTexture

expect class Texture2D : ResourceImpl {
    val gl: GLTexture
}