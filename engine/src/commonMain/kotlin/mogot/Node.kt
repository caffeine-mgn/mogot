package mogot

import mogot.math.Matrix4fc
import pw.binom.io.Closeable


open class Node:Closeable {
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
        //NOP
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

    internal open fun update(delta:Float){
        behaviour?.onUpdate(delta)
        childs.forEach {
            it.update(delta)
        }
    }

    fun removeChild(node: Node) {
        node._parent = null
        _childs.remove(node)
    }

    fun getNode(name: String) =
            childs.find { it.name == name } ?: TODO("Can't find node with name \"$name\"")

    var visible: Boolean = false

    open fun apply(matrix: Matrix4fc): Matrix4fc = matrix
    open fun render(model: Matrix4fc, projection: Matrix4fc, renderContext: RenderContext) {

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

    var name: String = ""
}

fun Node.walk(func: (Node) -> Boolean) {
    if (func(this))
        childs.forEach {
            it.walk(func)
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