package mogot

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertNull

class NodeTest {
    private var counter = 0
    private fun genName() = "node${counter++}"

    @Test
    fun findNodeTest() {
        val root = Node()
        val child1 = Node()
        child1.parent = root
        child1.id = genName()

        val child2 = Node()
        child2.id = genName()
        child2.parent = child1

        root.findNode(child1.id!!)!!.eq(child1)
        assertNull(root.findNode(child2.id!!))
        root.findNode(child2.id!!, true)!!.eq(child2)
    }
}