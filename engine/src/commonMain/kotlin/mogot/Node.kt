package mogot

import mogot.math.Matrix4fc
import pw.binom.io.Closeable

open class Node : Closeable {
    open val type
        get() = 0
    var id: String? = null
    private var _parent: Node? = null
    private val _childs = ArrayList<Node>()
    val childs: List<Node>
        get() = _childs

    open fun getField(name: String): Field? = null

    var behaviour: Behaviour? = null
        set(value) {
            field = value
            if (value?._node != null)
                value.onStop()
            value?._node = this
            value?.onStart()
        }

    /**
     * Sets [parent]=null and then calls [close]
     */
    fun free() {
        parent = null
        close()
    }

    /**
     * Calls for all childs method [close]
     */
    override fun close() {
        childs.forEach {
            it.close()
        }
    }

    open var parent: Node?
        get() = _parent
        set(value) {
            _parent?.removeChild(this)
            value?.addChild(this)
        }

    fun addChild(node: Node) {
        node._parent?.removeChild(node)
        node._parent = this
        _childs.add(node)
    }

    fun setChildIndex(child: Node, index: Int) {
        if (index < 0)
            throw IllegalArgumentException("Index can't be less than 0")
        if (index >= childs.size)
            throw IllegalArgumentException("Index can't be grate or equals than total child count (${childs.size})")
        if (child.parent !== this)
            throw IllegalArgumentException("Child must have this node as parent")
        val index1 = childs.indexOfFirst { it === child }
        check(index >= 0) { "Can't find Child node in Child List" }
        _childs.removeAt(index1)
        _childs.add(index, child)
    }

    open fun update(delta: Float) {
        behaviour?.onUpdate(delta)
        childs.forEach {
            it.update(delta)
        }
    }

    fun removeChild(node: Node) {
        node._parent = null
        _childs.remove(node)
    }

    fun findNode(id: String, recursive: Boolean = false): Node? {
        if (recursive) {
            childs.forEach {
                if (it.id == id)
                    return it
                val result = it.findNode(id, true)
                if (result != null)
                    return result
            }
            return null
        } else {
            return childs.find { it.id == id }
        }
    }

    open fun apply(matrix: Matrix4fc): Matrix4fc = matrix
//    open fun render(model: Matrix4fc, projection: Matrix4fc, renderContext: RenderContext) {
//        //NOP
//    }

    protected open fun onStart() {
        childs.forEach {
            it.onStart()
        }
    }

    protected open fun onStop() {
        childs.forEach {
            it.onStop()
        }
    }

    internal fun callStart() {
        onStart()
    }

    internal fun callStop() {
        onStop()
    }
}

fun Node.walk(func: (Node) -> Boolean) {
    if (func(this))
        childs.forEach {
            it.walk(func)
        }
}

/**
 * Called [func] for all node from current to each child recursive until [func] does not return false.
 */
fun Node.currentToChilds(func: (Node) -> Boolean) {
    var node: Node? = this
    if (func(this))
        return
    childs.forEach {
        it.currentToChilds(func)
    }
}

/**
 * Called [func] for all node from root to current until [func] does not return false.
 * Function [currentToRoot] work to revers direction.
 */
fun Node.rootToCurrent(func: (Node) -> Unit) {
    parent?.rootToCurrent(func)
    func(this)
}

inline fun Node.isChild(node: Node) = node.isParent(this)

fun Node.isParent(node: Node): Boolean {
    parent?.currentToRoot {
        if (it == node)
            return true
        true
    }
    return false
}

/**
 * Called [func] for all node from current to root until [func] does not return false
 * Function [rootToCurrent] work to revers direction.
 */
inline fun Node.currentToRoot(func: (Node) -> Boolean) {
    var node: Node? = this
    while (node != null) {
        if (!func(node))
            break
        node = node.parent
    }
}

fun Node.fullPath(): List<Node> {
    val list = ArrayList<Node>()
    currentToRoot { list.add(it) }
    list.reverse()
    return list
}

fun Node.asUpSequence() = object : Sequence<Node> {
    override fun iterator(): Iterator<Node> = NodeParentIterator(parent)
}

private class NodeParentIterator(var node: Node?) : Iterator<Node> {

    override fun hasNext(): Boolean = node != null

    override fun next(): Node {
        val r = node ?: throw IllegalStateException()
        node = r.parent
        return r
    }
}

fun <T : Node> T.parent(parent: Node): T {
    this.parent = parent
    return this
}

fun Node.findByRelative(path: String): Node? {
    var node = this
    path.splitToSequence('/').forEach {
        if (it == ".")
            return@forEach
        if (it == "..") {
            node = node.parent ?: return null
            return@forEach
        }
        node = node.findNode(it) ?: return null
    }
    return node
}

