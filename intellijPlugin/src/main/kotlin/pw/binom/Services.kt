package pw.binom

import kotlin.reflect.KProperty

object Services {

    class Service<T : Any>(val obj: T)

    private val services = HashMap<String, Service<*>>()
    private var nameIt = 0
    fun <T : Any> reg(obj: T) = reg(name = "bean#${++nameIt}", obj = obj)

    fun <T : Any> reg(name: String, obj: T) {
        if (services.containsKey(name))
            throw IllegalArgumentException("Service with name $name already exist")
        services[name] = Service(obj = obj)
    }

    fun <T : Any> byClass(clazz: Class<T>) = ByClass(clazz)
    fun <T : Any> byClassOrNull(clazz: Class<T>) = ByClassOrNull(clazz)

    fun <T : Any> byClassSequence(clazz: Class<T>) = ListByClass(clazz)

    class ByClass<T : Any>(private val clazz: Class<T>) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
            val beans = services.values.filter { clazz.isInstance(it.obj) }
            if (beans.isEmpty())
                throw IllegalStateException("No such Service with class ${clazz.simpleName}")
            if (beans.size > 1)
                throw IllegalStateException("Added ${beans.size} with type ${clazz.simpleName}")
            return beans[0].obj as T
        }
    }

    class ByClassOrNull<T : Any>(private val clazz: Class<T>) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): T? {
            val beans = services.values.filter { clazz.isInstance(it.obj) }
            if (beans.isEmpty())
                return null
            if (beans.size > 1)
                throw IllegalStateException("Added ${beans.size} with type ${clazz.simpleName}")
            return beans[0].obj as T
        }
    }

    class ListByClass<T : Any>(private val clazz: Class<T>) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): Sequence<T> {
            return services.values.asSequence().filter { clazz.isInstance(it.obj) }.map { it.obj as T }
        }
    }
}