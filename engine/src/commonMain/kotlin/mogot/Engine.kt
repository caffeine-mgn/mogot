package mogot

import pw.binom.Stack
import pw.binom.io.Closeable
import pw.binom.io.FileSystem
import pw.binom.start
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class Engine constructor(val stage: Stage, fileSystem: FileSystem<Unit>) : Closeable {
    val gl
        get() = stage.gl
    val resources = Resources(this, fileSystem)
    val frameListeners = Stack<() -> Unit>()
    private val managers = HashMap<String, Closeable>()
    fun <T : Closeable> manager(name: String, factory: () -> T): T {
        return managers.getOrPut(name) { factory() } as T
    }

    override fun close() {
        managers.values.forEach {
            it.close()
        }
    }
}

/**
 * Ожидает потока отрисовки. За тем продолжает выполнение корутины
 */
suspend fun Engine.waitFrame() {
    suspendCoroutine<Unit> {
        frameListeners.pushLast {
            it.resume(Unit)
        }
    }
}

/**
 * Ожидает потока отрисовки. За тем выполняет функцию [func]
 */
fun Engine.waitFrame(func: suspend () -> Unit) {
    frameListeners.pushLast {
        func.start()
    }
}