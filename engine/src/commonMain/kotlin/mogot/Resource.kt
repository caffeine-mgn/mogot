package mogot

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