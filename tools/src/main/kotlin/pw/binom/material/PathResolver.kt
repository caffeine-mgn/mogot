package pw.binom.material

/**
 * Material file provider
 */
interface PathResolver {
    fun getFiles(packageName: String): Collection<FSElement>?
    fun getFile(path: String): MaterialSource?
    fun getPackage(path: String): MaterialPackage?
}

interface MaterialSource : FSElement {
    fun getSource(): String
}

interface MaterialPackage : FSElement

interface FSElement {
    val path: String
}