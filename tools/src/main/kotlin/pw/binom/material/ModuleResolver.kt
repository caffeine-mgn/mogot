package pw.binom.material

interface ModuleResolver {
    fun getModule(currentPath:String, path: String): Module?
}