package mogot.gl

import mogot.SourceImage
import pw.binom.io.Closeable

/**
 * @param data must be closed manually!!!
 */
class TextureObject(val gl: GL, image:SourceImage, val minFilter: MinFilterParameter = MinFilterParameter.Linear, val magFilter: MagFilterParameter = MagFilterParameter.Linear, val textureWrapS: TextureWrap = TextureWrap.Repeat, val textureWrapT: TextureWrap = TextureWrap.Repeat, val multisample: MSAALevels = MSAALevels.Disable, val format: Format = Format.RGB, val mipMaps: Int = 0) : Closeable {
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

    val glTexture: GLTexture = gl.createTexture()
    val target = if (multisample != MSAALevels.Disable) gl.TEXTURE_2D_MULTISAMPLE else gl.TEXTURE_2D

    constructor(gl: GL, width:Int, height:Int, minFilter: MinFilterParameter = MinFilterParameter.Linear, magFilter: MagFilterParameter = MagFilterParameter.Linear, textureWrapS: TextureWrap = TextureWrap.Repeat, textureWrapT: TextureWrap = TextureWrap.Repeat, multisample: MSAALevels = MSAALevels.Disable, format: Format = Format.RGB, mipMaps: Int = 0):this(gl,SourceImage(if(format==Format.RGBA) SourceImage.Type.RGBA else SourceImage.Type.RGB,width,height,null))

    init {
        gl.enable(gl.TEXTURE_2D)
        gl.checkError {
            "Can't enable texture"
        }
        bind()
        if (multisample == MSAALevels.Disable) {
            gl.texParameteri(target, gl.TEXTURE_MIN_FILTER, when (minFilter) {
                MinFilterParameter.Nearest -> gl.NEAREST
                MinFilterParameter.Linear -> gl.LINEAR
                MinFilterParameter.NearestMipmapNearest -> gl.NEAREST_MIPMAP_NEAREST
                MinFilterParameter.LinearMipmapNearest -> gl.LINEAR_MIPMAP_NEAREST
                MinFilterParameter.NearestMipmapLinear -> gl.NEAREST_MIPMAP_LINEAR
                MinFilterParameter.LinearMipmapLinear -> gl.LINEAR_MIPMAP_LINEAR
            })
            gl.checkError {
                "Can't set texture params"
            }
            gl.texParameteri(target, gl.TEXTURE_MAG_FILTER, when (magFilter) {
                MagFilterParameter.Nearest -> gl.NEAREST
                MagFilterParameter.Linear -> gl.LINEAR
            })
            gl.checkError {
                "Can't set texture params"
            }
            gl.texParameteri(target, gl.TEXTURE_WRAP_S, when (textureWrapS) {
                TextureWrap.ClampToEdge -> gl.CLAMP_TO_EDGE
                TextureWrap.MirroredRepeat -> gl.MIRRORED_REPEAT
                TextureWrap.Repeat -> gl.REPEAT
            })
            gl.checkError {
                "Can't set texture params"
            }
            gl.texParameteri(target, gl.TEXTURE_WRAP_T, when (textureWrapT) {
                TextureWrap.ClampToEdge -> gl.CLAMP_TO_EDGE
                TextureWrap.MirroredRepeat -> gl.MIRRORED_REPEAT
                TextureWrap.Repeat -> gl.REPEAT
            })
            gl.checkError {
                "Can't set texture params"
            }
            if((minFilter!=MinFilterParameter.Nearest)&&(minFilter!=MinFilterParameter.Linear)) {
                gl.texParameteri(target, gl.TEXTURE_MAX_LEVEL, mipMaps)
                //TODO("glGenerateMipmap(GL_TEXTURE_2D);")
                gl.checkError {
                    "Can't set texture params"
                }
            }
        }


        if (multisample != MSAALevels.Disable) {
            gl.texImage2DMultisample(gl.TEXTURE_2D_MULTISAMPLE, multisample.level, when(format){
                Format.RGB -> gl.RGB
                Format.RGBA -> gl.RGBA
                Format.DEPTH_COMPONENT -> gl.DEPTH_COMPONEN
            }, image.width, image.height, true)
        } else {
            gl.texImage2D(gl.TEXTURE_2D, 0, when(format){
                Format.RGB -> gl.RGB
                Format.RGBA -> gl.RGBA
                Format.DEPTH_COMPONENT -> gl.DEPTH_COMPONEN
            }, image.width, image.height, 0, gl.RGB, gl.UNSIGNED_BYTE, image.data)
        }
        gl.checkError {
            "Can't create texture"
        }
        unbind()
    }

    fun bind() {
        gl.bindTexture(target, glTexture)
    }

    fun unbind() {
        gl.bindTexture(target, null)
    }

    override fun close() {
        gl.disable(target)
    }

    fun enable() {
        if (multisample != MSAALevels.Disable)
            gl.enable(gl.MULTISAMPLE)
    }

    fun disable() {
        if (multisample != MSAALevels.Disable)
            gl.disable(gl.MULTISAMPLE)
    }


}