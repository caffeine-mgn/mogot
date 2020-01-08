package mogot

import pw.binom.Stack
import pw.binom.io.Closeable

abstract class ObjectPool<T : Any> : Closeable {
    private val stack = ArrayList<T>()
    protected abstract fun create(): T
    private var closed = false

    fun poll(): T {
        check(!closed) { "Object Pool closed" }
        if (stack.isEmpty())
            return create()
        return stack.removeAt(stack.lastIndex)
    }

    fun push(value: T) {
        check(!closed) { "Object Pool closed" }
        stack.add(value)
    }

    override fun close() {
        check(!closed) { "Object Pool already closed" }
        stack.clear()
        closed = true
    }
}