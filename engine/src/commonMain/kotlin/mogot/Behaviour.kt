package mogot

import mogot.math.*
import mogot.math.set
import mogot.physics.d2.Contact2D
import kotlin.reflect.KProperty
/*
private class Vec3Property(override val name: String) : Behaviour.PropertyRef<Vector3fm> {
    val vec3 = Vector3f()
    override fun getValue(thisRef: Behaviour, property: KProperty<*>): Vector3fm = vec3
    override val type: Field.Type
        get() = Field.Type.VEC3
    override var value: Any
        get() = vec3
        set(value) {
            value as Vector3fc
            vec3.set(value)
        }
}

private class Vec4Property(override val name: String) : Behaviour.PropertyRef<Vector4fm> {
    val vec4 = Vector4f()
    override fun getValue(thisRef: Behaviour, property: KProperty<*>): Vector4fm = vec4
    override val type: Field.Type
        get() = Field.Type.VEC4
    override var value: Any
        get() = vec4
        set(value) {
            value as Vector4fc
            vec4.set(value)
        }
}

private class Vec2Property(override val name: String) : Behaviour.PropertyRef<Vector2fm> {
    val vec2 = Vector2f()
    override fun getValue(thisRef: Behaviour, property: KProperty<*>): Vector2fm = vec2
    override val type: Field.Type
        get() = Field.Type.VEC2
    override var value: Any
        get() = vec2
        set(value) {
            value as Vector2fc
            vec2.set(value)
        }
}

private class FloatProperty(override val name: String) : Behaviour.PropertyValue<Float> {
    private var float = 0f
    override fun getValue(thisRef: Behaviour, property: KProperty<*>): Float = float
    override val type: Field.Type
        get() = Field.Type.FLOAT

    override var value: Any
        get() = float
        set(value) {
            float = value as Float
        }

    override fun setValue(thisRef: Behaviour, property: KProperty<*>, value: Float) {
        float = value
    }
}
*/
abstract class Behaviour {

    interface PropertyRef<T> : Field {
        operator fun getValue(thisRef: Behaviour, property: KProperty<*>): T
    }

    interface PropertyValue<T> : Field {
        operator fun getValue(thisRef: Behaviour, property: KProperty<*>): T
        operator fun setValue(thisRef: Behaviour, property: KProperty<*>, value: T)
    }

    private val _properties = HashMap<String, Field>()
    val properties: Collection<Field>
        get() = _properties.values

    internal var _node: Node? = null
        set(value) {
            checkNode(value)
            field = value
        }
    protected open val node
        get() = _node!!

    protected open fun checkNode(node: Node?) {
        //
    }
/*
    protected fun propertyVec3(name: String, x: Float = 0f, y: Float = 0f, z: Float = 0f): PropertyRef<Vector3fm> {
        val property = Vec3Property(name)
        property.vec3.set(x, y, z)
        _properties[name] = property
        return property
    }

    protected fun propertyVec2(name: String, x: Float = 0f, y: Float = 0f): PropertyRef<Vector2fm> {
        val property = Vec2Property(name)
        property.vec2.set(x, y)
        _properties[name] = property
        return property
    }

    protected fun propertyVec4(name: String, x: Float = 0f, y: Float = 0f, z: Float = 0f, w: Float = 0f): PropertyRef<Vector4fm> {
        val property = Vec4Property(name)
        property.vec4.set(x, y, z, w)
        _properties[name] = property
        return property
    }

    protected fun propertyFloat(name: String, initValue: Float = 0f): PropertyValue<Float> {
        val property = FloatProperty(name)
        property.value = initValue
        _properties[name] = property
        return property
    }

    fun getField(name: String): Field? = _properties[name]
*/
    open fun onStart() {
        //
    }

    open fun onStop() {

    }

    open fun onUpdate(delta: Float) {

    }

    open fun onCollisionEnter2D(contact: Contact2D) {

    }

    open fun onCollisionLeave2D(contact: Contact2D) {

    }
}