package pw.binom.utils

import mogot.math.*
import org.jbox2d.dynamics.BodyType
import javax.swing.SwingUtilities


fun <T> Sequence<T>.equalsAll(): Boolean {
    val first = firstOrNull() ?: return true
    return all { it == first }
}

fun <T> Sequence<T>.equalsAllBy(func: (T) -> Any?): Boolean {
    val first = firstOrNull() ?: return true
    val value = func(first)
    return all { func(it) == value }
}

val <T> Sequence<T>.isEmpty: Boolean
    get() = !iterator().hasNext()

val <T> Sequence<T>.isNotEmpty: Boolean
    get() = !isEmpty

val Sequence<Float>.common: Float
    get() {
        if (isEmpty)
            return Float.NaN

        val it = iterator()
        var vec = it.next()
        while (it.hasNext()) {
            val p = it.next()
            vec = if (!vec.isNaN() && p == vec) vec else Float.NaN
        }
        return vec
    }

val Sequence<BodyType>.common: BodyType?
    get() {
        if (isEmpty)
            return null

        val it = iterator()
        val vec = it.next()
        while (it.hasNext()) {
            val p = it.next()
            if (p != vec)
                return null
        }
        return vec
    }

val <T : Vector2fc> Sequence<T>.common: Vector2f
    get() {
        if (isEmpty)
            return Vector2f(Float.NaN, Float.NaN)

        val it = iterator()
        val vec = Vector2f(it.next())
        while (it.hasNext()) {
            val p = it.next()
            vec.x = if (!vec.x.isNaN() && p.x == vec.x) vec.x else Float.NaN
            vec.y = if (!vec.y.isNaN() && p.y == vec.y) vec.y else Float.NaN
        }
        return vec
    }

val <T : Vector3fc> Sequence<T>.common: Vector3f
    get() {
        if (isEmpty)
            return Vector3f(Float.NaN, Float.NaN, Float.NaN)

        val it = iterator()
        val vec = Vector3f(it.next())
        while (it.hasNext()) {
            val p = it.next()
            vec.x = if (!vec.x.isNaN() && p.x == vec.x) vec.x else Float.NaN
            vec.y = if (!vec.y.isNaN() && p.y == vec.y) vec.y else Float.NaN
            vec.z = if (!vec.z.isNaN() && p.z == vec.z) vec.z else Float.NaN
        }
        return vec
    }

class Vector2fmDelegator(val vector: Vector2fm, val updateEvent: () -> Unit) : Vector2fm {
    override var x: Float
        get() = vector.x
        set(value) {
            vector.x = value
            updateEvent()
        }
    override var y: Float
        get() = vector.y
        set(value) {
            vector.y = value
            updateEvent()
        }

    override fun set(x: Float, y: Float): Vector2fm {
        vector.set(x, y)
        updateEvent()
        return this
    }
}

fun executeOnUiThread(func: () -> Unit) {
    if (SwingUtilities.isEventDispatchThread()) {
        println("Execute in UI")
        func()
        return
    }
    println("Execute later")
    SwingUtilities.invokeLater { func() }
}