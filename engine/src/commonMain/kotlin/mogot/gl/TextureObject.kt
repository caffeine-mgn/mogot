package mogot.gl

import mogot.SourceImage
import pw.binom.io.Closeable

class TextureObject(val glcontext: GL, image:SourceImage, val minFilter: MinFilterParameter = MinFilterParameter.Linear, val magFilter: MagFilterParameter = MagFilterParameter.Linear, val textureWrapS: TextureWrap = TextureWrap.Repeat, val textureWrapT: TextureWrap = TextureWrap.Repeat, val multisample: MSAALevels = MSAALevels.Disable, val format: Format = Format.RGB, val mipMaps: Int = 0) : Closeable {
    enum class MagFilterParameter {
        Nearest,
        Linear
    }
    enum class Format{
        RGB,
        RGBA,
        DEPTH_COMPONENT
    }

    enum class MinFilterParameter {
        Nearest,
        Linear,
        NearestMipmapNearest,
        LinearMipmapNearest,
        NearestMipmapLinear,
        LinearMipmapLinear
    }

    enum class TextureWrap {
        ClampToEdge,
        MirroredRepeat,
        Repeat
    }

    enum class MSAALevels(val level: Int) {
        Disable(0),
        MSAAx4(4),
        MSAAx8(8),
        MSAAx16(16)
    }

    val gl: GLTexture = glcontext.createTexture()
    val target = if (multisample != MSAALevels.Disable) glcontext.TEXTURE_2D_MULTISAMPLE else glcontext.TEXTURE_2D

    constructor(gl: GL, width:Int, height:Int, minFilter: MinFilterParameter = MinFilterParameter.Linear, magFilter: MagFilterParameter = MagFilterParameter.Linear, textureWrapS: TextureWrap = TextureWrap.Repeat, textureWrapT: TextureWrap = TextureWrap.Repeat, multisample: MSAALevels = MSAALevels.Disable, format: Format = Format.RGB, mipMaps: Int = 0):this(gl,SourceImage(if(format==Format.RGBA) SourceImage.Type.RGBA else SourceImage.Type.RGB,width,height,null),minFilter,magFilter,textureWrapS,textureWrapT,multisample,format, mipMaps)

    init {
        glcontext.enable(glcontext.TEXTURE_2D)
        glcontext.checkError {
            "Can't enable texture"
        }
        bind()
        if (multisample == MSAALevels.Disable) {
            glcontext.texParameteri(target, glcontext.TEXTURE_MIN_FILTER, when (minFilter) {
                MinFilterParameter.Nearest -> glcontext.NEAREST
                MinFilterParameter.Linear -> glcontext.LINEAR
                MinFilterParameter.NearestMipmapNearest -> glcontext.NEAREST_MIPMAP_NEAREST
                MinFilterParameter.LinearMipmapNearest -> glcontext.LINEAR_MIPMAP_NEAREST
                MinFilterParameter.NearestMipmapLinear -> glcontext.NEAREST_MIPMAP_LINEAR
                MinFilterParameter.LinearMipmapLinear -> glcontext.LINEAR_MIPMAP_LINEAR
            })
            glcontext.checkError {
                "Can't set texture params"
            }
            glcontext.texParameteri(target, glcontext.TEXTURE_MAG_FILTER, when (magFilter) {
                MagFilterParameter.Nearest -> glcontext.NEAREST
                MagFilterParameter.Linear -> glcontext.LINEAR
            })
            glcontext.checkError {
                "Can't set texture params"
            }
            glcontext.texParameteri(target, glcontext.TEXTURE_WRAP_S, when (textureWrapS) {
                TextureWrap.ClampToEdge -> glcontext.CLAMP_TO_EDGE
                TextureWrap.MirroredRepeat -> glcontext.MIRRORED_REPEAT
                TextureWrap.Repeat -> glcontext.REPEAT
            })
            glcontext.checkError {
                "Can't set texture params"
            }
            glcontext.texParameteri(target, glcontext.TEXTURE_WRAP_T, when (textureWrapT) {
                TextureWrap.ClampToEdge -> glcontext.CLAMP_TO_EDGE
                TextureWrap.MirroredRepeat -> glcontext.MIRRORED_REPEAT
                TextureWrap.Repeat -> glcontext.REPEAT
            })
            glcontext.checkError {
                "Can't set texture params"
            }
            glcontext.texParameteri(target, glcontext.TEXTURE_MAX_LEVEL, mipMaps)
            glcontext.generateMipmap(glcontext.TEXTURE_2D)
        }


        if (multisample != MSAALevels.Disable) {
            glcontext.texImage2DMultisample(glcontext.TEXTURE_2D_MULTISAMPLE, multisample.level, when(format){
                Format.RGB -> glcontext.RGB
                Format.RGBA -> glcontext.RGBA
                Format.DEPTH_COMPONENT -> glcontext.DEPTH_COMPONEN
            }, image.width, image.height, true)
        } else {
            glcontext.texImage2D(glcontext.TEXTURE_2D, 0, when(format){
                Format.RGB -> glcontext.RGB
                Format.RGBA -> glcontext.RGBA
                Format.DEPTH_COMPONENT -> glcontext.DEPTH_COMPONEN
            }, image.width, image.height, 0, glcontext.RGB, glcontext.UNSIGNED_BYTE, image.data)
        }
        glcontext.checkError {
            "Can't create texture"
        }
        unbind()
    }

    fun bind() {
        glcontext.bindTexture(target, gl)
    }

    fun unbind() {
        glcontext.bindTexture(target, null)
    }

    override fun close() {
        glcontext.disable(target)
    }

    fun enable() {
        if (multisample != MSAALevels.Disable)
            glcontext.enable(glcontext.MULTISAMPLE)
    }

    fun disable() {
        if (multisample != MSAALevels.Disable)
            glcontext.disable(glcontext.MULTISAMPLE)
    }


}