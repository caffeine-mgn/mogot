package mogot

import pw.binom.io.Closeable

class ResourceManager : Closeable {
    private val files = HashMap<String, Resource>()

    suspend fun <T : Resource> get(name: String, put: suspend () -> T?): T? {
        var res = files[name]
        if (res == null) {
            res = put()
            if (res != null) {
                files[name] = res
                res.disposeListener = {
                    files.remove(name)
                }
            }
        }
        return res as T?
    }

    override fun close() {
        files.values.toTypedArray().forEach {
            it.dec()
        }
    }
}

val Resources.manager: ResourceManager
    get() = engine.manager("ResourceManager") { ResourceManager() }