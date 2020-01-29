package mogot.physics.box2d.dynamics

import mogot.physics.box2d.box2d
import mogot.physics.box2d.mogot

actual typealias Body = org.jbox2d.dynamics.Body

actual fun Body.setType(type: BodyType) {
    this.type = type.box2d
}

actual typealias BodyType = org.jbox2d.dynamics.BodyType


actual fun Body.getType(): BodyType = this.type.mogot