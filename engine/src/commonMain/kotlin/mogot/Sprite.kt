package mogot

import mogot.material.DEFAULT_MATERIAL_2D_FILE
import mogot.material.MaterialInstance
import mogot.material.loadMaterial
import mogot.math.*
import pw.binom.async

private object SizeField : AbstractField<Sprite, Vector2fc>() {
    override val name: String
        get() = "size"

    override val type: Field.Type
        get() = Field.Type.VEC2

    override suspend fun setValue(engine: Engine, node: Sprite, value: Vector2fc) {
        node.size.set(value)
    }

    override fun currentValue(node: Sprite): Vector2fc = node.size
}

private object TextureFileField : AbstractField<Sprite, String>() {
    override val name: String
        get() = "texture"

    override val type: Field.Type
        get() = Field.Type.STRING

    override suspend fun setValue(engine: Engine, node: Sprite, value: String) {
        node.texture = if (value.isEmpty()) null else node.engine.resources.createTexture2D(value)
        node.currentFile = value
    }

    override fun currentValue(node: Sprite): String = node.currentFile ?: ""
}

abstract class AbstractSprite(engine: Engine) : VisualInstance2D(engine) {

    companion object {

        fun calcPolygonTriangulation(vertex: List<Vector2fc>): List<Int> {
            val array = DoubleArray(vertex.size * 2)
            vertex.forEachIndexed { index, vector2fc ->
                array[index * 2 + 0] = vector2fc.x.toDouble()
                array[index * 2 + 1] = vector2fc.y.toDouble()
            }
            return Earcut.earcut(array, null, 2)
        }
    }

    internal var currentFile: String? = null

    private var geom by ResourceHolder<Rect2D>()
    open val size: Vector2fm = Vector2f()

    override fun getField(name: String): Field? =
            when (name) {
                SizeField.name -> SizeField
                TextureFileField.name -> TextureFileField
                else -> super.getField(name)
            }

    private var oldSize: Vector2f? = null

    protected abstract val material: Material

    protected abstract val isReady: Boolean
    abstract var texture: Texture2D?

    override fun render(model: Matrix4fc, projection: Matrix4fc, renderContext: RenderContext) {
        if (!isReady)
            return

        if (geom == null) {
            geom = Rect2D(engine.gl, size)
        }

        if (oldSize == null) {
            oldSize = Vector2f(size)
        }

        if (size != oldSize) {
            geom!!.size.set(size)
            oldSize!!.set(size)
        }
        super.render(model, projection, renderContext)
        val mat = material

        mat.use(model, projection, renderContext)
        geom!!.draw()
        mat.unuse()
    }

    override fun close() {
        geom = null
        super.close()
    }
}

open class Sprite(engine: Engine) : AbstractSprite(engine) {
    override var texture: Texture2D? = null
        set(value) {
            if (value === field)
                return
            field?.dec()
            field = value
            field?.inc()
            if (value != null)
                _material?.set("image", value)
            else
                _material?.remove("image")
        }
    private var _material: MaterialInstance? = null
    override val material: Material
        get() = _material!!
    override var isReady: Boolean = false

    init {
        async {
            _material = engine.resources.loadMaterial("$DEFAULT_MATERIAL_2D_FILE.mat").instance()
            if (texture != null) {
                _material!!.set("image", texture!!)
            }
            isReady = true
        }
    }
}