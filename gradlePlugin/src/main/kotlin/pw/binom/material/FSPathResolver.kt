package pw.binom.material

import java.io.File
import java.io.FileInputStream

class FSPathResolver(val rootPath: File) : PathResolver {
    override fun getFiles(packageName: String): Collection<FSElement>? {
        val f = File(rootPath, packageName)
        if (!f.isDirectory)
            return null
        return f.listFiles()?.asSequence()?.map {
            if (it.isFile) {
                if (it.extension.toLowerCase() == "mat")
                    FSMaterialSource(this, it)
                else
                    null
            } else {
                FSMaterialSource(this, it)
            }
        }?.filterNotNull()?.toSet() ?: emptyList()
    }

    override fun getFile(path: String): MaterialSource? {
        val f = File(rootPath, path)
        if (!f.isFile)
            return null
        if (f.extension.toLowerCase() != "mat")
            return null
        return FSMaterialSource(this, f)
    }

    override fun getPackage(path: String): MaterialPackage? {
        val f = File(rootPath, path)
        if (!f.isDirectory)
            return null
        return FSMaterialPackage(this, f)
    }

}

class FSMaterialSource(private val pathResolver: FSPathResolver, val file: File) : MaterialSource {
    init {
        if (file.isFile)
            throw IllegalArgumentException("File ${file.absolutePath} not exist")
    }

    override fun getSource(): String =
            FileInputStream(file).bufferedReader().use {
                it.readText()
            }

    override val path: String
        get() = file.toURI().relativize(pathResolver.rootPath.toURI()).path
}

class FSMaterialPackage(private val pathResolver: FSPathResolver, val file: File) : MaterialPackage {
    init {
        if (!file.isDirectory)
            throw IllegalArgumentException("Directory ${file.absolutePath} not exist")
    }
    override val path: String
        get() = file.toURI().relativize(pathResolver.rootPath.toURI()).path
}