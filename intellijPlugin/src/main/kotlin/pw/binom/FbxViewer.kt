package pw.binom

import mogot.*
import mogot.fbx.FbxImporter
import java.io.ByteArrayInputStream

class FbxViewer(private val file: ByteArray) : View3D() {
    override fun init() {
        super.init()
        val model = FbxImporter.import(ByteArrayInputStream(file))
        val fbx = FbxImporter.import(gl, model) { SimpleMaterial(gl) }
//        val node1 = GeomNode(Geoms.solidSphere(gl, 1f, 30, 30), SimpleMaterial(gl))
//        val node2 = GeomNode(Geoms.solidSphere(gl, 1f, 30, 30), SimpleMaterial(gl))
//        root!!.addChild(node1)
//        root!!.addChild(node2)
//        node2.position.x = -5f
        val l = OmniLight()
        val bb = CSGBox(Engine(this))
        bb.width = 1f
        bb.depth = 1f
        bb.height = 1f
        bb.material = SimpleMaterial(gl)
//        node1.position.z = -30f
//        node1.position.x = 30f
//        node1.position.y = 30f
//        node1.addChild(l)
        l.position.set(-30f,30f,30f)
        l.parent=root
        root!!.addChild(fbx)
        root!!.addChild(bb)
        resetCam()
    }

    override fun dispose() {
        super.dispose()
        root!!.childs.toTypedArray().forEach {
            if (it !is Camera)
                it.parent = null
        }
    }

}