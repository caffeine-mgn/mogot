package mogot.fbx

interface FbxObject {
    val id: Long
    fun connectTo(obj: FbxObject, param:String?)
}