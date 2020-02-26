package pw.binom.sceneEditor

import mogot.math.Vector4f
import mogot.physics.box2d.dynamics.BodyType
private const val alpha=0.3f
private const val hover=0.5f
private const val selected=0.7f

class Settings {


    val shapeDynamicColor = Vector4f(1f, 0f, 0f, alpha)
    val shapeDynamicHoverColor = Vector4f(1f, 0f, 0f, hover)
    val shapeDynamicSelectedColor = Vector4f(1f, 0f, 0f, selected)

    val shapeStaticColor = Vector4f(1f, 0f, 0f, alpha)
    val shapeStaticHoverColor = Vector4f(1f, 0f, 0f, hover)
    val shapeStaticSelectedColor = Vector4f(1f, 0f, 0f, selected)

    val shapeKinimaticColor = Vector4f(1f, 0f, 0f, alpha)
    val shapeKinimaticHoverColor = Vector4f(1f, 0f, 0f, hover)
    val shapeKinimaticSelectedColor = Vector4f(1f, 0f, 0f, selected)

    val shapeSimpleColor = Vector4f(1f, 0f, 0f, alpha)
    val shapeSimpleHoverColor = Vector4f(1f, 0f, 0f, hover)
    val shapeSimpleSelectedColor = Vector4f(1f, 0f, 0f, selected)

    fun getShapeColor(bodyType: BodyType?, hover: Boolean, selected: Boolean) =
            when (bodyType) {
                BodyType.DYNAMIC -> when {
                    selected -> shapeDynamicSelectedColor
                    hover -> shapeDynamicHoverColor
                    else -> shapeDynamicColor
                }
                BodyType.STATIC -> when {
                    selected -> shapeStaticSelectedColor
                    hover -> shapeStaticHoverColor
                    else -> shapeStaticColor
                }
                BodyType.KINEMATIC -> when {
                    selected -> shapeKinimaticSelectedColor
                    hover -> shapeKinimaticHoverColor
                    else -> shapeKinimaticColor
                }
                else -> when {
                    selected -> shapeSimpleSelectedColor
                    hover -> shapeSimpleHoverColor
                    else -> shapeSimpleColor
                }
            }

}