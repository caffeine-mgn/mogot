package pw.binom.fbx.file

class FbxGeometry(val el: FbxRoot.Element) : FbxObject {

    val vertices = el.get("Vertices").first().properties[0] as DoubleArray
    val edges = el.get("Edges").first().properties[0] as IntArray
    val polygonVertexIndex = el.get("PolygonVertexIndex").first().properties[0] as IntArray
    val normals = el.get("LayerElementNormal").first().get("Normals").first().properties[0] as DoubleArray
    val uiElements = el.get("LayerElementUV").first().get("UV").first().properties[0] as DoubleArray
    val uiIndex = el.get("LayerElementUV").first().get("UVIndex").first().properties[0] as IntArray

    val name: String by lazy {
        getName(el) ?: ""
    }

    companion object {
        fun getName(el: FbxRoot.Element): String? {
            var id = el.properties.getOrNull(1) as? String ?: return null
            for (i in id.indices) {
                if (id.codePointAt(i) == 0) {
                    id = id.substring(0, i)
                    break
                }
            }
            return id
        }
    }

    override val id: Long
        get() = el.properties[0] as Long

    var skin: FbxSkin? = null

    override fun connectTo(obj: FbxObject, param: String?) {
        if (obj is FbxModel2) {
            obj.geometry = this
            return
        }
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}