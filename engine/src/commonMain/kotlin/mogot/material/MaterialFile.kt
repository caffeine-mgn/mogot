package mogot.material

import mogot.*
import mogot.gl.MaterialGLSL
import mogot.gl.Shader
import mogot.math.Matrix4fc
import mogot.math.Vector3fc
import mogot.math.Vector4fc
import mogot.rendering.Display
import pw.binom.asUTF8String
import pw.binom.io.*

const val DEFAULT_MATERIAL_2D_FILE = "Material2DDefault"

class MaterialInstance(val material: ExternalMaterialGLSL) : Material, ResourceImpl() {

    private val activeTextures = ArrayList<Texture2D>()
    private val textureIds = HashMap<Texture2D, Int>()

    private var Texture2D.id2: Int
        get() = textureIds[this] ?: -1
        set(value) {
            textureIds[this] = value
        }

    private fun updateTextureIndexes() {
        activeTextures.forEachIndexed { index, textureFile ->
            textureFile.id2 = index
        }
    }

    fun remove(name: String) {
        val old = params.remove(name)
        if (old is Texture2D) {
            activeTextures -= old
            old.dec()
            needUpdateTextureIndex = true
        }
    }

    fun set(name: String, value: Any) {
        val old = params[name]
        if (old === value)
            return
        if (old is Texture2D) {
            activeTextures -= old
            old.dec()
            needUpdateTextureIndex = true
        }
        if (value is Texture2D) {
            value.inc()
            activeTextures += value
            needUpdateTextureIndex = true
        }
        params[name] = value
    }

    private var needUpdateTextureIndex = false

    private inline val gl
        get() = material.gl.gl

    private val params = HashMap<String, Any>()
    override fun use(model: Matrix4fc, projection: Matrix4fc, context: Display.Context) {
        material.use(model, projection, context)
        params.forEach { (k, v) ->
            when (v) {
                is Vector3fc -> material.shader.uniform(k, v)
                is Vector4fc -> material.shader.uniform(k, v)
                is Matrix4fc -> material.shader.uniform(k, v)
                is Int -> material.shader.uniform(k, v)
                is Float -> material.shader.uniform(k, v)
                is Texture2D -> {
                    if (needUpdateTextureIndex) {
                        updateTextureIndexes()
                        needUpdateTextureIndex = false
                    }
                    gl.activeTexture(gl.TEXTURE0 + v.id2)
                    gl.bindTexture(gl.TEXTURE_2D, v.gl)
                    material.shader.uniform(k, v.id2)
                }
            }
        }
    }

    init {
        material.inc()
    }

    override fun dispose() {
        material.dec()
        super.dispose()
    }

    override fun unuse() {
        material.unuse()
    }

}

class ExternalMaterialGLSL(engine: Engine, vp: String, fp: String) : MaterialGLSL(engine) {
    override val shader = Shader(engine.gl, vp, fp)
    fun instance() = MaterialInstance(this)
}

object MaterialLoader {
    const val GLES300_VP = "glsl300_vp"
    const val GLES300_FP = "glsl300_fp"
    suspend fun read(engine: Engine, stream: AsyncInputStream): ExternalMaterialGLSL {
        var gles300Vp: String? = null
        var gles300Fp: String? = null
        val keyCount = stream.readInt()
        println("Reading material properties. Count: $keyCount")
        (0 until keyCount).forEach {
            val name = stream.readUTF8String()
            val blockSize = stream.readInt()
            val block = ByteArray(blockSize)
            stream.readFully(block)

            when (name) {
                GLES300_VP -> gles300Vp = block.asUTF8String()
                GLES300_FP -> gles300Fp = block.asUTF8String()
                else -> println("Unknown property: $name")
            }
        }
        return ExternalMaterialGLSL(engine, gles300Vp!!, gles300Fp!!)
    }
}

suspend fun Resources.loadMaterial(path: String): ExternalMaterialGLSL =
        manager.get(path) {
            val entity = fileSystem.get(Unit, path + ".bin") ?: throw FileSystem.FileNotFoundException(path)
            entity.loadMaterial(engine)
        }!!

suspend fun <U> FileSystem.Entity<U>.loadMaterial(engine: Engine): ExternalMaterialGLSL {
    val stream = this.read() ?: throw FileSystem.FileNotFoundException(this.path)
    return stream.use {
        MaterialLoader.read(engine, it)
    }
}