package pw.binom.utils

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import mogot.Node
import mogot.fullPath
import mogot.math.*
import org.jbox2d.dynamics.BodyType
import javax.swing.SwingUtilities
import kotlin.reflect.KProperty


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

val Sequence<String>.common: String?
    get() {
        if (isEmpty)
            return null

        val it = iterator()
        var vec = it.next()
        while (it.hasNext()) {
            val p = it.next()
            if (vec != p)
                return null
        }
        return vec
    }

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

val Sequence<Boolean>.common: Boolean?
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

val Sequence<Int>.common: Int?
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

val <T : Quaternionfc> Sequence<T>.common: Quaternionf
    get() {
        if (isEmpty)
            return Quaternionf(Float.NaN, Float.NaN, Float.NaN)

        val it = iterator()
        val vec = Quaternionf(it.next())
        while (it.hasNext()) {
            val p = it.next()
            vec.x = if (!vec.x.isNaN() && p.x == vec.x) vec.x else Float.NaN
            vec.y = if (!vec.y.isNaN() && p.y == vec.y) vec.y else Float.NaN
            vec.z = if (!vec.z.isNaN() && p.z == vec.z) vec.z else Float.NaN
            vec.w = if (!vec.w.isNaN() && p.w == vec.w) vec.w else Float.NaN
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

class Vector3fmDelegator(val vector: Vector3fm, val updateEvent: () -> Unit) : Vector3fm {
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
    override var z: Float
        get() = vector.z
        set(value) {
            vector.z = value
            updateEvent()
        }

    override fun set(x: Float, y: Float, z: Float): Vector3fm {
        vector.set(x, y, z)
        updateEvent()
        return this
    }
}

class QuaternionfmDelegator(val vector: Quaternionfm, val updateEvent: () -> Unit) : Quaternionfm {
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
    override var z: Float
        get() = vector.z
        set(value) {
            vector.z = value
            updateEvent()
        }

    override var w: Float
        get() = vector.w
        set(value) {
            vector.w = value
            updateEvent()
        }

    override fun set(x: Float, y: Float, z: Float, w: Float) {
        vector.set(x, y, z, w)
        updateEvent()
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

fun String.json() = JsonNodeFactory.instance.textNode(this)
fun Int.json() = JsonNodeFactory.instance.numberNode(this)
fun Float.json() = JsonNodeFactory.instance.numberNode(this)

fun Map<String, JsonNode?>.json(): ObjectNode {
    val node = JsonNodeFactory.instance.objectNode()
    node.setAll<JsonNode>(node)
    return node
}

fun json(vararg values: Pair<String, JsonNode?>): ObjectNode {
    val node = JsonNodeFactory.instance.objectNode()
    values.forEach {
        node.set<JsonNode>(it.first, it.second)
    }
    return node
}

fun Collection<JsonNode>.json() = JsonNodeFactory.instance.arrayNode(size).addAll(this)
fun <T : JsonNode> Iterator<T>.json(): ArrayNode {
    val node = JsonNodeFactory.instance.arrayNode()
    forEach {
        node.add(it)
    }
    return node
}

fun <T, R> Iterator<T>.map(mapper: (T) -> R) = object : Iterator<R> {
    override fun hasNext(): Boolean = this@map.hasNext()
    override fun next(): R = mapper(this@map.next())
}

fun Node.relativePath(otherNode: Node): String? {
    val thisPath = this.fullPath()
    val otherPath = otherNode.fullPath()
    var thisIndex = 0
    var otherIndex = 0
    while (true) {
        if (thisPath.size > thisIndex && otherPath.size > otherIndex && thisPath[thisIndex] === otherPath[otherIndex]) {
            thisIndex++
            otherIndex++
        } else {
            break
        }
    }
    val sb = StringBuilder()
    if (thisIndex < thisPath.size) {
        sb.append("../")
        while (thisPath[thisIndex] !== this) {
            thisIndex++
            sb.append("../")
        }
    }
    (otherIndex until otherPath.size).forEach {
        if (!sb.endsWith("/") && sb.isNotEmpty())
            sb.append("/")
        sb.append(otherPath[it].id ?: return null)
    }
    return sb.toString()
}

class Vector3mDegrees(val vector: Vector3fm) : Vector3fm {
    override var x: Float
        get() = toDegrees(vector.x)
        set(value) {
            vector.x = toRadians(value)
        }
    override var y: Float
        get() = toDegrees(vector.y)
        set(value) {
            vector.y = toRadians(value)
        }
    override var z: Float
        get() = toDegrees(vector.z)
        set(value) {
            vector.z = toRadians(value)
        }

    override fun set(x: Float, y: Float, z: Float): Vector3fm {
        vector.set(toRadians(x), toRadians(y), toRadians(z))
        return this
    }
}