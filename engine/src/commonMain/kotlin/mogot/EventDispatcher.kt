package mogot

import pw.binom.io.Closeable

class EventValueDispatcher<T> {
    private val listeners = ArrayList<(T) -> Unit>()
    fun on(listener: (T) -> Unit): Closeable {
        listeners += listener
        return object : Closeable {
            override fun close() {
                listeners -= listener
            }

        }
    }

    fun dispatch(value: T) {
        listeners.forEach {
            it(value)
        }
    }
}

class EventDispatcher {
    private val listeners = ArrayList<() -> Unit>()
    fun on(listener: () -> Unit): Closeable {
        listeners += listener
        return object : Closeable {
            override fun close() {
                listeners -= listener
            }

        }
    }

    fun dispatch() {
        listeners.forEach {
            it()
        }
    }
}