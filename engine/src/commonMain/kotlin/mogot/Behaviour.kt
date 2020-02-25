package mogot

import mogot.physics.d2.Contact2D

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

    open fun onCollisionEnter2D(contact: Contact2D) {

    }

    open fun onCollisionLeave2D(contact: Contact2D) {

    }
}