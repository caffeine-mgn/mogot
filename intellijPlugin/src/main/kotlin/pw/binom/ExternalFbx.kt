package pw.binom

import com.intellij.openapi.vfs.VirtualFile
import mogot.*
import pw.binom.fbx.file.*
import pw.binom.io.Closeable

class ExternalFbx(val engine: Engine, val file: VirtualFile) : ResourceImpl() {

    var lastChange = 0L
        private set
    private var _meta: FbxMeta? = null

    val meta: FbxMeta?
        get() {
            checkChange()
            return _meta
        }

    private fun checkChange() {
        if (!file.exists() || file.isDirectory) {
            _meta = null
            geoms.clear()
            lastChange = 0
            return
        }
        if (_meta == null || file.modificationStamp > lastChange) {
            lastChange = file.modificationStamp
            _meta = file.inputStream.use { stream ->
                FbxImporter.import3(stream)
            }
            geoms.clear()
        }
    }

    private val geoms = HashMap<String, FbxGeometry>()
    fun findGeometry(path: String): FbxGeometry? {
        checkChange()
        val g = geoms[path]
        if (g != null)
            return g


        val geom = _meta?.geoms?.get(path)
                ?: return null

        val f = geom
        geoms[path] = f
        return f
    }

    private val geoms2 = HashMap<String, FbxGeom>()

    fun getGeom(name: String): FbxGeom {
        return geoms2.getOrPut(name) {
            val f = FbxGeom(engine, name, this)
            f.disposeListener = {
                geoms2.remove(name)
            }
            f
        }
    }
}

class ExternalFbxManager(val engine: Engine) : Closeable {
    private val files = HashMap<String, ExternalFbx>()
    fun loadFbx(file: VirtualFile) = files.getOrPut(file.path) {
        val f = ExternalFbx(engine, file)
        f.disposeListener = {
            files.remove(file.path)
        }
        f
    }

    override fun close() {
        files.clear()
    }

}

fun Resources.loadFbx(file: VirtualFile): ExternalFbx {
    val manager = engine.manager("FbxLoader") { ExternalFbxManager(engine) }
    return manager.loadFbx(file)
}

class FbxGeom(val engine: Engine, val name: String, val fbx: ExternalFbx) : Geometry, ResourceImpl() {
    override var mode: Geometry.RenderMode = Geometry.RenderMode.TRIANGLES

    private val geom = ResourceHolder<Geom3D2>()
    private var lastChange = fbx.lastChange

    private fun checkChanges() {
        if (geom.value != null && fbx.lastChange != 0L && fbx.lastChange <= lastChange)
            return
        val vv = fbx.findGeometry(name)
        if (vv == null) {
            geom.value = null
            return
        }
        lastChange = fbx.lastChange
        geom.value = FbxImporter.import2(engine.gl, vv)
    }

    init {
        fbx.inc()
    }

    override fun dispose() {
        geom.dispose()
        super.dispose()
        fbx.dec()
    }

    override fun draw() {
        checkChanges()
        geom.value?.draw()
    }

}