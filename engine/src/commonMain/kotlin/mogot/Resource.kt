package mogot

import kotlin.reflect.KProperty

interface Resource {
    /**
     * Must be call for increment link counter
     */
    fun inc()

    /**
     * Must be call for decrement link counter
     */
    fun dec(): Boolean

    /**
     * Dispose listener. Will called when resource disposed
     */
    var disposeListener: ((Resource) -> Unit)?
}

/**
 * Simple implement for [Resource]
 *
 * <pre>
 *     class MySuperCube(engine: Engine):CSGBox(engine), Resource by ResourceImpl()
 * </pre>
 */
abstract class ResourceImpl : Resource {
    private var counter: UInt = 0u
    final override fun inc() {
        counter++
    }

    final override fun dec(): Boolean {
        counter--
        if (counter <= 0u)
            dispose()
        return counter == 0u
    }

    override var disposeListener: ((Resource) -> Unit)? = null

    /**
     * Override this method for dispose all included resources. And after dispose all included resources
     * necessarily call super.dispose.
     */
    protected open fun dispose() {
        disposeListener?.invoke(this)
    }
}

/**
 * Struct for hold some Resource. Automatic call inc and dec on the resource when you try to set new value
 */
class ResourceHolder<T : Resource>(var init: T? = null) {
    fun dispose() {
        value = null
    }

    var value: T? = null
        set(value) {
            if (value === field)
                return
            field?.dec()
            field = value
            field?.inc()
        }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T? = value

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
        this.value = value
    }

    init {
        value = init
    }
}