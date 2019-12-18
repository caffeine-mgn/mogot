package mogot

interface MaterialNode {
    var material: Material?
}

class MaterialNodeImpl : MaterialNode {
    override var material: Material? = null
        set(value) {
            field?.dec()
            field = value
            value?.inc()
        }

}