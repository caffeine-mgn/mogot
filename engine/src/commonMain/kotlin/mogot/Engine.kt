package mogot

import pw.binom.Stack
import pw.binom.start
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class Engine constructor(val stage: Stage) {
    val gl
        get() = stage.gl
    val resources = Resources(this)
    val frameListeners = Stack<() -> Unit>()
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