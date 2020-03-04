package pw.binom

import mogot.*
import pw.binom.fbx.file.FbxImporter
import java.io.ByteArrayInputStream

class FbxViewer(private val file: ByteArray) : View3D() {
    override fun init() {
        super.init()
        val model = FbxImporter.import(ByteArrayInputStream(file))
        val fbx = FbxImporter.import(gl, model) { SimpleMaterial(engine) }
        val l = PointLight()
        val bb = CSGBox(engine)
        bb.width = 1f
        bb.depth = 1f
        bb.height = 1f
        bb.material.value = SimpleMaterial(engine)
        l.position.set(-30f, 30f, 30f)
        l.parent = root
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