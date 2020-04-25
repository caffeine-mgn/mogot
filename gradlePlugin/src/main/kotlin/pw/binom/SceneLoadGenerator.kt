package pw.binom

object SceneLoadGenerator {
    fun generate(nodes: Set<String>, out: Appendable) {
        out
                .append("package mogot.scene\n\n")
//                .append("import mogot.scene.Loader\n\n")
                .append("private object InternalSceneLoader:Loader(){\n")

        out.append("\toverride fun newInstance(className: String, engine: Engine): Node =\n")
        out.append("\t\twhen (className) {\n")
        nodes.forEach {
            out.append("\t\t\t\"$it\" -> $it(engine)\n")
        }
        out.append("\t\t}")

        out.append("suspend fun Resources.loadScene(stream: AsyncInputStream) = InternalSceneLoader.loadScene(stream, engine)")
    }
}