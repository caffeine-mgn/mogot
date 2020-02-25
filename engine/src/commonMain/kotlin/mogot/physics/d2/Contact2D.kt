package mogot.physics.d2

import mogot.physics.d2.shapes.Shape2D

interface Contact2D {
    val shapeA: Shape2D
    val shapeB: Shape2D
}