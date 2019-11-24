package mogot.fbx

class FbxGeometry(val el: FbxFile.Element) : FbxObject {

    val vertices = el.get("Vertices").first().properties[0] as DoubleArray
    val edges = el.get("Edges").first().properties[0] as IntArray
    val polygonVertexIndex = el.get("PolygonVertexIndex").first().properties[0] as IntArray
    val normals = el.get("LayerElementNormal").first().get("Normals").first().properties[0] as DoubleArray
    val uiElements = el.get("LayerElementUV").first().get("UV").first().properties[0] as DoubleArray
    val uiIndex = el.get("LayerElementUV").first().get("UVIndex").first().properties[0] as IntArray

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