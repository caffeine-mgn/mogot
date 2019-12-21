package pw.binom.material

import pw.binom.io.file.FileNotFoundException
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

class ZipPathResolver(val file: File) : PathResolver {
    val zipFile = ZipFile(file)

    override fun getFiles(packageName: String): Collection<FSElement>? =
            zipFile.entries()
                    .asSequence()
                    .filter {
                        if (!it.name.startsWith(packageName))
                            return@filter false
                        "/" !in it.name.removePrefix(packageName)
                    }
                    .map {
                        if (it.isDirectory)
                            ZipMaterialPackage(this, it)
                        else {
                            if (it.name.endsWith(".mat"))
                                ZipMaterialSource(this, it)
                            else
                                null
                        }
                    }.filterNotNull().toSet()

    override fun getFile(path: String): MaterialSource? {
        val d = zipFile.getEntry(path) ?: throw FileNotFoundException("File $path not found in ${file.absolutePath}")
        return ZipMaterialSource(this, d)
    }

    override fun getPackage(path: String): MaterialPackage? {
        val d = zipFile.getEntry(path)
                ?: throw FileNotFoundException("Directory $path not found in ${file.absolutePath}")
        return ZipMaterialPackage(this, d)
    }

}

class ZipMaterialSource(private val pathResolver: ZipPathResolver, val entry: ZipEntry) : MaterialSource {
    init {
        if (entry.isDirectory)
            throw IllegalArgumentException("File ${entry.name} is directory")
    }

    override fun getSource(): String =
            pathResolver.zipFile.getInputStream(entry).bufferedReader().use {
                it.readText()
            }

    override val path: String
        get() = entry.name
}

class ZipMaterialPackage(private val pathResolver: ZipPathResolver, val entry: ZipEntry) : MaterialPackage {
    override val path: String
        get() = entry.name
}