package pw.binom.sceneEditor.struct

import mogot.Node
import mogot.asUpSequence
import javax.swing.tree.TreePath

fun Node.makeTreePath(): TreePath {
    val bb = this.asUpSequence().filter { it.parent != null }.toList().reversed() + this
    return TreePath(bb.toTypedArray())
}

fun TreePath.next(obj: Any): TreePath = NextTreePath(this, obj)

private class NextTreePath(parent: TreePath, next: Any) : TreePath(parent, next)