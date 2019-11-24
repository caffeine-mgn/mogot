package mogot

abstract class Behaviour {
    internal var _node: Node? = null
        set(value) {
            checkNode(value)
            field = value
        }
    protected open val node
        get() = _node!!

    protected open fun checkNode(node: Node?) {
        //
    }

    open fun onStart() {
        //
    }

    open fun onStop() {

    }

    open fun onUpdate(delta: Float) {

    }
}