package mogot.fbx

class FbxFile : FbxVisiter {
    private var _version = 0u
    val version: UInt
        get() = _version

    class Element(val id: String, val properties: List<Any>, val childs: List<Element>) {
        fun get(id: String) = childs.asSequence().filter { it.id == id }
        fun double(index:Int)=properties[index] as Double
        fun string(index:Int)=properties[index] as String
        fun long(index:Int)=properties[index] as Long
    }

    fun get(id: String) = elements.asSequence().filter { it.id == id }

    private val rootElements = ArrayList<Element>()
    val elements: List<Element>
        get() = rootElements

    companion object {
        private fun subElement(parent: SubElementVisiter?, name: String, f: (Element) -> Unit) = SubElementVisiter(name, f)
        val SECONDS_PER_UNIT = 1 / 46186158000.0
    }

    private class SubElementVisiter(val name: String, val f: (Element) -> Unit) : ElementVisiter {
        private val properties = ArrayList<Any>()
        private val childs = ArrayList<Element>()
        override fun element(id: String): ElementVisiter? = subElement(this, id) { this.childs += it }

        override fun property(value: Any) {
            properties += value
        }

        override fun elementEnd() {
            f(Element(name, properties, childs))
        }

    }


    override fun element(id: String): ElementVisiter = subElement(null, id) { rootElements += it }

    override fun start() {
    }

    override fun version(version: UInt) {
        this._version = version
    }

    override fun end() {
    }

}