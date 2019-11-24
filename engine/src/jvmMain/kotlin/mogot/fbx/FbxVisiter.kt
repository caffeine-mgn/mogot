package mogot.fbx

interface ElementContener {
    fun element(id: String): ElementVisiter?
}

interface ElementVisiter : ElementContener {
    fun property(value: Any)
    fun elementEnd()
}

interface FbxVisiter : ElementContener {
    fun start()
    fun version(version: UInt)
    fun end()
}