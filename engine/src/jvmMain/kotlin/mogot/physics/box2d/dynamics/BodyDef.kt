package mogot.physics.box2d.dynamics

actual typealias BodyDef = org.jbox2d.dynamics.BodyDef

actual var BodyDef.type: BodyType
    get() = BodyType.valueOf(type.name)
    set(value) {
        this.type = org.jbox2d.dynamics.BodyType.valueOf(value.name)
    }