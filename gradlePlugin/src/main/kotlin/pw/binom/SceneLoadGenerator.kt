package pw.binom

object SceneLoadGenerator {
    fun generate(nodes: Set<String>, out: Appendable) {
        out
                .append("package mogot.scene\n")
                .append("import mogot.Engine\n")
                .append("import mogot.Node\n")
                .append("import mogot.Resources\n")
                .append("import pw.binom.io.AsyncInputStream\n\n")
                .append("private object InternalSceneLoader:Loader(){\n")

        out.append("\toverride fun newInstance(className: String, engine: Engine): Node =\n")
        out.append("\t\twhen (className) {\n")
        nodes.forEach {
            out.append("\t\t\t\"$it\" -> $it(engine)\n")
        }
        out.append("\t\t\telse -> TODO()\n")
        out.append("\t\t}\n")
        out.append("}\n")

        out.append("suspend fun Resources.loadScene(stream: AsyncInputStream, into: Node = Node()) = InternalSceneLoader.loadScene(stream, engine, into)\n")
        out.append("suspend fun Resources.loadScene(path: String, into: Node = Node()) = fileSystem.get(Unit, path+\".bin\")?.read()?.let { loadScene(it, into) }\n")
    }
}