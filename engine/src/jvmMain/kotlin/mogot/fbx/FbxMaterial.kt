package mogot.fbx

class FbxMaterial(val el: FbxFile.Element) : FbxObject {
    override val id: Long
        get() = el.properties[0] as Long

    override fun connectTo(obj: FbxObject, param: String?) {
        if (obj is FbxModel2) {
            obj.material = this
            return
        }

        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}