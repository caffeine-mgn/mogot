package mogot

import pw.binom.io.Closeable
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

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

@UseExperimental(ExperimentalContracts::class)
inline fun <T : Any, R> ObjectPool<T>.use1(func: (T) -> R): R {
    contract {
        callsInPlace(func, InvocationKind.EXACTLY_ONCE)
    }
    val v = poll()
    return try {
        func(v)
    } finally {
        push(v)
    }
}

@UseExperimental(ExperimentalContracts::class)
inline fun <T : Any, R> ObjectPool<T>.use2(func: (T, T) -> R): R {
    contract {
        callsInPlace(func, InvocationKind.EXACTLY_ONCE)
    }
    val v1 = poll()
    val v2 = poll()
    return try {
        func(v1, v2)
    } finally {
        push(v1)
        push(v2)
    }
}

@UseExperimental(ExperimentalContracts::class)
inline fun <T : Any, R> ObjectPool<T>.use3(func: (T, T, T) -> R): R {
    contract {
        callsInPlace(func, InvocationKind.EXACTLY_ONCE)
    }
    val v1 = poll()
    val v2 = poll()
    val v3 = poll()
    return try {
        func(v1, v2, v3)
    } finally {
        push(v1)
        push(v2)
        push(v3)
    }
}