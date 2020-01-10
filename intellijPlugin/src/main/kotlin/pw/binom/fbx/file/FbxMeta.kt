package pw.binom.fbx.file

class FbxMeta(geoms: List<FbxGeometry>, val childs: List<FbxModel2>) {
    val geoms = geoms.asSequence().associate { it.name to it }
}