package mogot

/*
open class BaseNode3D : Node() {
    val position = Vector3f()
    //    override val rotation = Quaternionf()
//    val rotation2 = Vector3f()

    val scale = Vector3f(1f, 1f, 1f)

//    var render3D: Render3D? = null
//        set(value) {
//            field?.unusedNode()
//            field = value
//            field?.usedNode(this)
//        }

    override fun render(model: Matrix4fc, projection: Matrix4fc, renderContext: RenderContext) {
//        render3D?.render(projection, model,renderContext)
    }

//    var collider: Collider? = null
//        set(value) {
//            field?.node = null
//            field = value
//            field?.node = this
//        }

    override open fun free() {
        parent = null
        while (childs.isNotEmpty())
            childs.first().free()
    }

//    private val _up = Vector3f(0f, 1f, 0f)
//    val up: Vector3fc
//        get() = _up


    protected var _matrix = Matrix4f()

    val matrix: Matrix4fc
        get() = _matrix

    //    private val _direction = Vector3f()
    val quaternion = Quaternionf()

//    private fun update() {
//        quaternion.identity()
////        quaternion.rotateXYZ(rotation2.x, rotation2.y, rotation2.z)
//        quaternion.rotateZYX(rotation2.z, rotation2.y, rotation2.x)
////        quaternion.setRotation(rotation2.z, rotation2.y, rotation2.x)
////        quaternion.rotateEuler(rotation2.y, rotation2.z, rotation2.x, quaternion)
//        _direction.set(quaternion.forward)
//    }

//    val right: Vector3fc
//        get() {
//            update()
//            return quaternion.left
//        }
//
//    val left: Vector3fc
//        get() {
//            update()
//            return quaternion.left
//        }
//
//    val direction: Vector3fc
//        get() {
//            update()
//            return quaternion.forward
//        }

    override fun apply(matrix: Matrix4fc): Matrix4fc {
//        update()
        this._matrix.set(matrix)
        this._matrix.translate(position)
//        this._matrix.rotateAffineZYX(rotation2.z,rotation2.y,rotation2.x)
        this._matrix.rotate(quaternion)
        this._matrix.scale(scale)

        return this._matrix
    }

    /**
     * Итератор по родителям
     */
    val parentIterator: Iterator<Node>
        get() = NodeIterator(parent)

    private class NodeIterator(var node: Node?) : Iterator<Node> {

        override fun hasNext(): Boolean = node != null

        override fun next(): Node {
            val r = node ?: throw IllegalStateException()
            node = r.parent
            return r
        }
    }
}

//fun <T : BaseNode3D> T.use(render: Render3D): T {
//    if (this.render3D == null) {
//        this.render3D = render
//    } else {
//        if (this.render3D is MultiRender3D) {
//            (this.render3D as MultiRender3D).use(render)
//        } else {
//            val r = this.render3D!!
//            val r2 = MultiRender3D()
//            this.render3D = r2
//            r2.use(r)
//            r2.use(render)
//        }
//    }
//    return this
//}
/*
fun Quaternionfc.rotateEuler(heading: Float, attitude: Float, bank: Float, dest: Quaternionf) {
    val c1 = Math.cos(heading.toDouble()).toFloat()
    val s1 = Math.sin(heading.toDouble()).toFloat()
    val c2 = Math.cos(attitude.toDouble()).toFloat()
    val s2 = Math.sin(attitude.toDouble()).toFloat()
    val c3 = Math.cos(bank.toDouble()).toFloat()
    val s3 = Math.sin(bank.toDouble()).toFloat()
    dest.w = (Math.sqrt(1.0 + c1 * c2 + c1 * c3 - s1 * s2 * s3 + c2 * c3) / 2.0).toFloat()
    val w4 = 4.0f * dest.w
    dest.x = (c2 * s3 + c1 * s3 + s1 * s2 * c3) / w4
    dest.y = (s1 * c2 + s1 * c3 + c1 * s2 * s3) / w4
    dest.z = (-s1 * s3 + c1 * s2 * c3 + s2) / w4

}
*/