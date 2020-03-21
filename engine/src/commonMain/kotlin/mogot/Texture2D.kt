package mogot

import mogot.gl.GLTexture
import mogot.gl.TextureObject

class Texture2D(private var textureObject: TextureObject) : ResourceImpl() {
    constructor(engine: Engine, image: SourceImage) : this(TextureObject(engine.gl, image,
            TextureObject.MinFilterParameter.LinearMipmapLinear,
            TextureObject.MagFilterParameter.Linear,
            TextureObject.TextureWrap.Repeat,
            TextureObject.TextureWrap.Repeat,
            TextureObject.MSAALevels.Disable,
            when (image.type) {
                SourceImage.Type.RGB -> TextureObject.Format.RGB
                SourceImage.Type.RGBA -> TextureObject.Format.RGBA
            },
            3
    ))

    val gl:GLTexture
        get() = textureObject.gl
    fun getTextureObject() = textureObject
    override fun dispose() {
        textureObject.close()
        super.dispose()
    }

}