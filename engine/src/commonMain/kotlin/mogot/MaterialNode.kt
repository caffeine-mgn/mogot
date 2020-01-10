package mogot

interface MaterialNode {
    val material: ResourceHolder<Material>
}

class MaterialNodeImpl : MaterialNode {
    override val material = ResourceHolder<Material>()
}