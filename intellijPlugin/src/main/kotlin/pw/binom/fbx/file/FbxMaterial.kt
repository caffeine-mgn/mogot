package pw.binom.fbx.file

class FbxMaterial(val el: FbxRoot.Element) : FbxObject {
    override val id: Long
        get() = el.properties[0] as Long

    class Texture(val texture: FbxTexture, val useAs: String?)

    val textures = ArrayList<Texture>()

    override fun connectTo(obj: FbxObject, param: String?) {
        if (obj is FbxModel2) {
            obj.material = this
            return
        }

        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}