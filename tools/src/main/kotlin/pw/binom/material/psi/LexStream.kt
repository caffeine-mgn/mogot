package pw.binom.material.psi

import pw.binom.Stack

class LexStream<T>(private val func: () -> Element<T>?) {
    open class Element<T>(val element: T, val text: String, val line: Int, val column: Int,val position:Int)

    private val elements = ArrayList<Element<T>>()
    var cursor = 0
        private set

    private val stack = Stack<Int>()

    private fun pushState() {
        stack.pushLast(cursor)
    }

    private fun skipState() {
        stack.popLast()
    }

    private fun popState() {
        val cur = stack.popLast()
        cursor = cur
    }

    private var closed = false

    fun next(): Element<T>? {
        if (cursor == elements.size) {
            if (closed)
                return null

            val c = func()
            if (c == null) {
                closed = true
                return null
            }
            elements += c
            cursor++
            return c
        }
        return elements[cursor++]
    }

    fun <T : Any?> safe(func: () -> T): T {
        pushState()
        val r = func()
        if (r == null)
            popState()
        else
            skipState()
        return r
    }
}