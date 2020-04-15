package mogot
/*
import pw.binom.Stack
import kotlin.coroutines.suspendCoroutine

class FeaturePromise<T> {
    private var onResume = Stack<(Result<T>) -> Unit>()
    private var result: Result<T>? = null
    fun onResume(func: (Result<T>) -> Unit): FeaturePromise<T> {
        val result = this.result
        if (result != null)
            func(result)
        else
            onResume.pushFirst(func)
        return this
    }

    fun resume(result: Result<T>) {
        check(this.result == null)
        this.result = result
        while (!onResume.isEmpty) {
            onResume.popLast().invoke(result)
        }
    }
}

suspend fun <T> FeaturePromise<T>.await(): T =
        suspendCoroutine<T> {
            this.onResume { result ->
                it.resumeWith(result)
            }
        }

*/