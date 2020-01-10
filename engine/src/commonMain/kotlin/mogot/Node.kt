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

    var behaviour: Behaviour? = null
        set(value) {
            field = value
            if (value?._node != null)
                value.onStop()
            value?._node = this
            value?.onStart()
        }

    internal open fun free() {
        childs.forEach {
            it.free()
        }
    }

    override fun close() {
        childs.forEach {
            it.close()
        }
    }

    var parent: Node?
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

    internal open fun update(delta: Float) {
        behaviour?.onUpdate(delta)
        childs.forEach {
            it.update(delta)
        }
    }

    fun removeChild(node: Node) {
        node._parent = null
        _childs.remove(node)
    }

    fun findNode(id: String, recursive: Boolean = false): Node? =
            if (recursive) {
                var out: Node? = null
                childs.forEach {
                    currentToChilds {
                        if (it.id == id) {
                            out = it
                            false
                        }
                        true
                    }
                }
                out
            } else {
                childs.find { it.id == id }
            }

    open fun apply(matrix: Matrix4fc): Matrix4fc = matrix
    open fun render(model: Matrix4fc, projection: Matrix4fc, renderContext: RenderContext) {
        //NOP
    }

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