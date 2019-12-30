package pw.binom.fbx.file

interface FbxObject {
    val id: Long
    fun connectTo(obj: FbxObject, param:String?)
}