package pw.binom.material.lex

import pw.binom.Stack
import pw.binom.material.Module
import pw.binom.material.SourcePoint

class LexStream<T>(val module: Module, private val func: () -> Element<T>?) {
    open class Element<T>(val module: Module, val element: T, val text: String, val line: Int, val column: Int, val position: Int) {
        fun source() = SourcePoint(module, position, text.length)
    }

    private val elements = ArrayList<Element<T>>()
    var cursor = 0
        private set

    private val stack = Stack<Int>()

    fun pushState() {
        stack.pushLast(cursor)
    }

    fun skipState() {
        stack.popLast()
    }

    fun popState() {
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