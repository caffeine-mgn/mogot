package pw.binom.material

object EmptyModuleResolver : ModuleResolver {
    override fun getModule(currentPath:String, path: String): Module? = null
}