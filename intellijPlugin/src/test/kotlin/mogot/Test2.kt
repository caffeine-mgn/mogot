package mogot

import mogot.*
import org.junit.Assert
import org.junit.Test

class Test2 {
    fun node(vararg id: String): Node = node(null, *id)
    fun node(root: Node?, vararg id: String): Node {
        require(id.isNotEmpty())
        var parent: Node? = root
        id.forEach {
            val l = Node()
            l.id = it
            l.parent = parent
            parent = l
        }
        return parent!!
    }

    val Node.root: Node
        get() {
            var s = this
            while (true) {
                s = s.parent ?: return s
            }
            return s
        }

    @Test
    fun findByRelativeTest() {
        val root = node("1", "2", "3", "4").root
        val n2 = root.findNode("2", true)!!
        val n2_1 = node(n2, "2.1")
        val n2_2 = node(n2, "2.2")

        Assert.assertEquals(n2_2, n2_1.findByRelative("../2.2"))
    }

//    @Test
//    fun relativePathTest() {
//        val root = node("1", "2", "3", "4").root
//        val n2 = root.findNode("2", true)!!
//        val n2_1 = node(n2, "2.1")
//        val n2_2 = node(n2, "2.2")
//        Assert.assertEquals("2.2", n2.relativePath(n2_2))
//        Assert.assertEquals("../2.2", n2_1.relativePath(n2_2))
//    }
}