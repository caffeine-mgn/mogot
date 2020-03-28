package pw.binom.sceneEditor

import mogot.Material
import mogot.ResourceImpl
import mogot.math.*
import mogot.rendering.Display
import pw.binom.material.compiler.Compiler
import pw.binom.material.compiler.SingleType

class MaterialInstance(val root: ExternalMaterial) : Material, ResourceImpl() {

    var selected = false
    var hover = false

    init {
        root.inc()
    }

    enum class Type {
        Texture, Float, Int, Boolean, Vec2, Vec3, Vec4
    }

    inner class Uniform(
            val name: String,
            val title: String,
            val value: String?,
            val min: String?,
            val max: String?,
            val step: String?,
            val type: Type
    ) {
        val materialInstance
            get() = this@MaterialInstance

        override fun equals(other: Any?): Boolean {
            other ?: return false
            if (this === other)
                return true
            if (other is Uniform) {
                return other.name == name && other.type == type
            }
            return false
        }
    }

    private val _uniforms = ArrayList<Uniform>()

    private var oldCompiler: Compiler? = null
    private fun update() {
        _uniforms.clear()
        root.compiler.properties.forEach { (key, value) ->
            val type = when {
                key.type is SingleType && key.type.clazz.name == "sampler2D" -> Type.Texture
                key.type is SingleType && key.type.clazz.name == "int" -> Type.Int
                key.type is SingleType && key.type.clazz.name == "float" -> Type.Float
                key.type is SingleType && key.type.clazz.name == "bool" -> Type.Boolean
                key.type is SingleType && key.type.clazz.name == "vec2" -> Type.Vec2
                key.type is SingleType && key.type.clazz.name == "vec3" -> Type.Vec3
                key.type is SingleType && key.type.clazz.name == "vec4" -> Type.Vec4
                else -> null
            } ?: return@forEach

            _uniforms += Uniform(
                    name = key.name,
                    title = value["title"] ?: key.name,
                    type = type,
                    value = value["value"],
                    min = value["min"],
                    max = value["max"],
                    step = value["step"]
            )
        }
        val names = _uniforms.map { it.name }

        //Remove old values
        values.filter { value -> value.key !in names }.forEach { (key, value) ->
            values.remove(key)?.let { it as? ExternalTexture }?.let { it.dec() }
        }

        //Remote invalid values
        _uniforms.forEach {
            val value = values[it.name] ?: return@forEach
            when {
                value is Float && it.type != Type.Float
                        || value is Vector3f && it.type != Type.Vec3
                        || value is Vector4f && it.type != Type.Vec4
                        || value is Vector2f && it.type != Type.Vec2
                        || value is Boolean && it.type != Type.Boolean
                        || value is Int && it.type != Type.Int -> values.remove(it.name)
                value is ExternalTexture && it.type != Type.Texture -> {
                    value.dec()
                    values.remove(it.name)
                }
            }
        }

        //Set default value
        _uniforms.asSequence()
                .filter {
                    it.name !in values.keys
                }
                .filter { it.value != null }
                .forEach {
                    val value: Any? = when (it.type) {
                        Type.Float -> it.value!!.toFloatOrNull()
                        Type.Int -> it.value!!.toFloatOrNull()
                        Type.Boolean -> it.value == "true"
                        Type.Vec2 -> it.value!!.split(';').let { Vector2f(it[0].toFloat(), it[1].toFloat()) }
                        Type.Vec3 -> it.value!!.split(';').let { Vector3f(it[0].toFloat(), it[1].toFloat(), it[2].toFloat()) }
                        Type.Vec4 -> it.value!!.split(';').let { Vector4f(it[0].toFloat(), it[1].toFloat(), it[2].toFloat(), it[3].toFloat()) }
                        Type.Texture -> root.file.parent.findFileByRelativePath(it.value!!)?.let { root.gl.resources.loadTexture(it) }
                    }
                    set(it, value)
                }
        root.compiler
    }

    val uniforms: List<Uniform>
        get() {
            if (oldCompiler != root.compiler) {
                update()
                oldCompiler = root.compiler
            }
            return _uniforms
        }

    private val values = HashMap<String, Any>()

    fun set(uniform: Uniform, value: Any?) {
        val oldValue = values.remove(uniform.name)
        if (oldValue is ExternalTexture) {
            oldValue.dec()
        }
        if (value is ExternalTexture) {
            value.inc()
        }
        if (value != null)
            values[uniform.name] = value
    }

    fun get(uniform: Uniform) = values[uniform.name]

    override fun use(model: Matrix4fc, projection: Matrix4fc, context: Display.Context) {
        root.use(model, projection, context)
        root.shader.uniform("selected", selected)
        root.shader.uniform("hover", hover)
        values.forEach { (name, value) ->
            when (value) {
                is Int -> root.shader.uniform(name, value)
                is Float -> root.shader.uniform(name, value)
                is Vector3fc -> root.shader.uniform(name, value)
                is Vector3ic -> root.shader.uniform(name, value)
                is Vector4fc -> root.shader.uniform(name, value)
                is ExternalTexture -> {
                    root.gl.gl.activeTexture(root.gl.gl.TEXTURE0)
                    root.gl.gl.bindTexture(root.gl.gl.TEXTURE_2D, value.gl.gl)
                    root.shader.uniform(name, 0)
                }
                else -> throw IllegalStateException("Unknown uniform type ${value::class.java.name}")
            }
        }
    }

    override fun unuse() {
        root.unuse()
    }

    override fun dispose() {
        values.values.asSequence().mapNotNull { it as? ExternalTexture }.forEach { it.dec() }
        root.dec()
        super.dispose()
    }

}