package pw.binom.fbx.file

class FbxTexture(val el: FbxRoot.Element) : FbxObject {
    override val id: Long
        get() = el.properties[0] as Long
    val relativeFilename= el.childs.asSequence().find { it.id=="RelativeFilename" }?.properties?.firstOrNull()?.let { it as String }
    val fileName= el.childs.asSequence().find { it.id=="FileName" }?.properties?.firstOrNull()?.let { it as String }

    override fun connectTo(obj: FbxObject, param: String?) {
        if (obj is FbxMaterial) {
            obj.textures += FbxMaterial.Texture(this, param)
            return
        }
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}