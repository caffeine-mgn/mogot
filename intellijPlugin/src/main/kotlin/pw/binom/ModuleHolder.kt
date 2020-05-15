package pw.binom

import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import pw.binom.material.IdeModuleResolver
import java.util.*
import com.intellij.openapi.module.Module as IdeModule

object ModuleHolder {
    class Holder(val module: IdeModule) {

        val resolver = IdeModuleResolver(module, this)

        fun getRelativePath(file: VirtualFile): String {
            val contentRoots = ModuleRootManager.getInstance(module).contentRoots
            val roots = contentRoots
            val r = roots.asSequence()
                    .map {
                        if (!file.path.startsWith(it.path))
                            return@map null
                        file.path.removePrefix(it.path).removePrefix("/").removePrefix("\\")
                    }.filterNotNull().firstOrNull()
            if (r == null) {
                println("Can't find relative path for $file")
                println("roots: $roots")
                return file.path
            }
            return r
        }

        fun findFileByRelativePath(path: String): VirtualFile? {
            val contentRoots = ModuleRootManager.getInstance(module).contentRoots
            val r = contentRoots
                    .asSequence()
                    .mapNotNull {
                        it.findFileByRelativePath(path)
                    }
                    .firstOrNull()
            if (r == null) {
                println("Can't resolve $path roots: ${contentRoots.toList()}")
                return LocalFileSystem.getInstance().findFileByPath(path)
            }
            return r
        }
    }

    private val map = WeakHashMap<IdeModule, Holder>()
    fun getInstance(module: IdeModule) = map.getOrPut(module) { Holder(module) }
    fun getInstance(file: VirtualFile, project: Project) =
            getInstance(ModuleUtil.findModuleForFile(file, project)!!)
}