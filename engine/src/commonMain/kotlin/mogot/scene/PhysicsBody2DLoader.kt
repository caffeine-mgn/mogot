package mogot.scene

import mogot.Engine
import mogot.Node
import mogot.physics.box2d.dynamics.BodyType
import mogot.physics.d2.PhysicsBody2D

object PhysicsBody2DLoader : SceneLoader.NodeLoader {
    override val nodeClass: String
        get() = "mogot.physics.d2.PhysicsBody2D"

    override suspend fun load(engine: Engine, loaderContext: LoaderContext, props: Map<String, String>): Node {
        val node = PhysicsBody2D(engine)
        Spatial2DLoader.load(engine, node, loaderContext, props)
        node.bodyType = props["type"]?.let { BodyType.valueOf(it) } ?: BodyType.STATIC
        println("PhysicsBody2DLoader->${node.rotation}")
        return node
    }

}