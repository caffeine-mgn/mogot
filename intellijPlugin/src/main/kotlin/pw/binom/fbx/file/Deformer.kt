package pw.binom.fbx.file

class FbxCluster(val element: FbxRoot.Element) : FbxObject {
    override fun connectTo(obj: FbxObject, param: String?) {
        if (obj is FbxSkin) {
            obj.clusters += this
            return
        }
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    var bone: FbxModel2?=null

    override val id: Long
        get() = element.properties[0] as Long
    val indexes: IntArray
        get() = element.get("Indexes").first().properties[0] as IntArray

    val weights: DoubleArray
        get() = element.get("Weights").first().properties[0] as DoubleArray

}

class FbxSkin(val element: FbxRoot.Element) : FbxObject {
    override val id: Long
        get() = element.properties[0] as Long

    val clusters = ArrayList<FbxCluster>()

    override fun connectTo(obj: FbxObject, param: String?) {
        if (obj is FbxGeometry) {
            obj.skin = this
            return
        }
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}