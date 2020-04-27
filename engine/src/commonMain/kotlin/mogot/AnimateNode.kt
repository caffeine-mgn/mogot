package mogot

import mogot.math.*

private object AnimationListField3D : AbstractField<AnimateNode, String>() {
    override val type: Field.Type
        get() = Field.Type.FILE

    override val name: String
        get() = "animationList"

    override suspend fun setValue(engine: Engine, node: AnimateNode, value: String) {
        node.animations.clear()
        node.animations.addAll(value.split(Field.Type.LIST_SPLITOR))
    }

    override fun currentValue(node: AnimateNode): String = node.animations.joinToString(Field.Type.LIST_SPLITOR.toString())

    override suspend fun setSubFields(engine: Engine, node: Node, data: Map<String, Any>) {
        (data["animationIndex"] as Int?)?.let {
            node as AnimateNode
            node.setAnimation(it)
        }
    }
}

private object AnimationIndexListField3D : AbstractField<AnimateNode, Int>() {
    override val type: Field.Type
        get() = Field.Type.INT

    override val name: String
        get() = "animationIndex"

    override suspend fun setValue(engine: Engine, node: AnimateNode, value: Int) {
        node.setAnimation(value)
    }

    override fun currentValue(node: AnimateNode): Int = node.animationIndex
}

fun <T : Any> AnimationFile.Frame<T>.lerp(frameCount: Int, currentFrame: Float, next: AnimationFile.Frame<T>, lerpFunc: (T, T, Float) -> Unit) {
    if (next.time > time) {
        lerpFunc(value, next.value, currentFrame / (next.time - time))
    } else {
        val timeLerp = frameCount.toFloat() - time + next.time
        val currentLerp = frameCount.toFloat() - currentFrame + next.time
        lerpFunc(value, next.value, currentLerp / timeLerp)
    }
}

open class FrameHolder<T : Any>(val animation: AnimationFile.Property<T>) {
    var current = 0
    var next = 1

    private inline val Int.frame
        get() = animation.frames[this]

    fun frames(time: Float, revers: Boolean): Pair<AnimationFile.Frame<T>, AnimationFile.Frame<T>> {
        if (current.frame.time < next.frame.time && time >= current.frame.time && time < next.frame.time)
            return current.frame to next.frame
        if (current.frame.time > next.frame.time && time >= current.frame.time && current == animation.frames.lastIndex)
            return current.frame to next.frame
//        if (((current.frame.time < next.frame.time) && (time >= current.frame.time && time < next.frame.time)) ||
//                ((current.frame.time > next.frame.time) && (time <= current.frame.time && time < next.frame.time)))
//            return current.frame to next.frame

        fun Int.fix() = if (this >= animation.frames.size)
            0
        else
            this

        if (time > current.frame.time) {
            var i = next
            while (true) {
                if (i.frame.time <= time) {
                    current = i
                    next = (i + 1).fix()
                    break
                }
                i++
                if (i >= animation.frames.size) {
                    current = 0
                    next = 1
                    break
                }
            }
            return current.frame to next.frame
        } else {
            TODO()
        }
    }

    fun lerp(frame: Float, revers: Boolean, lerpFunc: (T, T, Float) -> Unit) {
        val frames = frames(frame, revers)
        if (frames.first.time < frames.second.time) {
            val fullTime = (frames.second.time - frames.first.time).toFloat()
            val time = frames.second.time - frame
            lerpFunc(
                    frames.first.value,
                    frames.second.value,
                    1f - time / fullTime
            )
        }
        if (frames.first.time > frames.second.time) {
            val fullTime = animation.file.frameCount - frames.first.time + frames.second.time
            val time = if (frame <= animation.file.frameCount) {
                animation.file.frameCount - frame + frames.second.time
            } else {
                animation.file.frameCount - frames.first.time + frame
            }
            lerpFunc(
                    frames.first.value, frames.second.value, 1f - time / fullTime
            )
        }
    }
}

class AnimateNode(val engine: Engine) : Node() {
    val animations = ArrayList<String>()

    override fun getField(name: String): Field? =
            when (name) {
                AnimationListField3D.name -> AnimationListField3D
                AnimationIndexListField3D.name -> AnimationIndexListField3D
                else -> super.getField(name)
            }

    sealed class AnimatedProperty<T : Any>(val animateNode: AnimateNode, val node: Node, val field: Field, animation: AnimationFile.Property<T>) : FrameHolder<T>(animation) {

        class Vec4(animateNode: AnimateNode, node: Node, field: Field, animation: AnimationFile.Property<Vector4fc>) : AnimatedProperty<Vector4fc>(animateNode, node, field, animation) {
            override fun playNext(frame: kotlin.Float, revers: Boolean) {
                TODO("Not yet implemented")
            }
        }

        class QuaternionfProp(animateNode: AnimateNode, node: Node, field: Field, animation: AnimationFile.Property<Quaternionfc>) : AnimatedProperty<Quaternionfc>(animateNode, node, field, animation) {
            override fun playNext(frame: kotlin.Float, revers: Boolean) {
                TODO("Not yet implemented")
            }
        }

        class Vec3(animateNode: AnimateNode, node: Node, field: Field, animation: AnimationFile.Property<Vector3fc>) : AnimatedProperty<Vector3fc>(animateNode, node, field, animation) {

            override fun playNext(frame: kotlin.Float, revers: Boolean) {
                TODO("Not yet implemented")
            }
        }

        class Vec2(animateNode: AnimateNode, node: Node, field: Field, animation: AnimationFile.Property<Vector2fc>) : AnimatedProperty<Vector2fc>(animateNode, node, field, animation) {
            var currentValue = Vector2f(field.get(node) as Vector2fc)
            var frame = animation.frames[0]
            var nextFrame = animation.frames[1]
            override fun playNext(frame: kotlin.Float, revers: Boolean) {
                lerp(frame, revers) { current, next, cof ->
                    current.lerp(next, cof, currentValue)
                }
                field.set(animateNode.engine, node, currentValue)
            }
        }

        class Int(animateNode: AnimateNode, node: Node, field: Field, animation: AnimationFile.Property<kotlin.Int>) : AnimatedProperty<kotlin.Int>(animateNode, node, field, animation) {
            override fun playNext(frame: kotlin.Float, revers: Boolean) {
                TODO("Not yet implemented")
            }
        }

        class Float(animateNode: AnimateNode, node: Node, field: Field, animation: AnimationFile.Property<kotlin.Float>) : AnimatedProperty<kotlin.Float>(animateNode, node, field, animation) {
            override fun playNext(frame: kotlin.Float, revers: Boolean) {
                lerp(frame, revers) { current, next, cof ->
                    val v = current.lerp(next, cof)
                    field.set(animateNode.engine, node, v)
                }
            }
        }

        class String(animateNode: AnimateNode, node: Node, field: Field, animation: AnimationFile.Property<kotlin.String>) : AnimatedProperty<kotlin.String>(animateNode, node, field, animation) {
            override fun playNext(frame: kotlin.Float, revers: Boolean) {
                TODO("Not yet implemented")
            }
        }

        class Bool(animateNode: AnimateNode, node: Node, field: Field, animation: AnimationFile.Property<Boolean>) : AnimatedProperty<Boolean>(animateNode, node, field, animation) {
            override fun playNext(frame: kotlin.Float, revers: Boolean) {
                TODO("Not yet implemented")
            }
        }

        abstract fun playNext(frame: kotlin.Float, revers: Boolean)
    }

    private var properties: Array<AnimatedProperty<out Any>> = emptyArray()
    var speed = 1f

    protected var internalCurrentAnimation by ResourceHolder<AnimationFile>()
    var currentAnimation: AnimationFile?
        get() = internalCurrentAnimation
        set(value) {
            if (value != null) {
                val animProperties = ArrayList<AnimatedProperty<out Any>>()
                value.objects.forEach { animObj ->
                    val node = this.findByRelative(animObj.path)
                    if (node == null) {
                        println("Can't find node ${animObj.path}")
                        return@forEach
                    }
                    animObj.properties.forEach { animProp ->
                        val field = node.getField(animProp.name)?.takeIf { it.type == animProp.type }
                        if (field == null) {
                            println("Can't find field ${animObj.path}::${animProp.name}")
                            return@forEach
                        }
                        val v = when (field.type) {
                            Field.Type.VEC3 -> AnimatedProperty.Vec3(this, node, field, animProp as AnimationFile.Property<Vector3fc>)
                            Field.Type.VEC2 -> AnimatedProperty.Vec2(this, node, field, animProp as AnimationFile.Property<Vector2fc>)
                            Field.Type.STRING -> AnimatedProperty.String(this, node, field, animProp as AnimationFile.Property<String>)
                            Field.Type.FILE -> AnimatedProperty.String(this, node, field, animProp as AnimationFile.Property<String>)
                            Field.Type.INT -> AnimatedProperty.Int(this, node, field, animProp as AnimationFile.Property<Int>)
                            Field.Type.BOOL -> AnimatedProperty.Bool(this, node, field, animProp as AnimationFile.Property<Boolean>)
                            Field.Type.FLOAT -> AnimatedProperty.Float(this, node, field, animProp as AnimationFile.Property<Float>)
                            Field.Type.VEC4 -> AnimatedProperty.Vec4(this, node, field, animProp as AnimationFile.Property<Vector4fc>)
                            Field.Type.QUATERNION -> AnimatedProperty.QuaternionfProp(this, node, field, animProp as AnimationFile.Property<Quaternionfc>)
                        }
                        animProperties += v
                    }
                }
                properties = animProperties.toTypedArray()
            }
            internalCurrentAnimation = value
        }

    var currentTime = 0f
    var revers = false

    override fun update(delta: Float) {
        val anim = currentAnimation
        if (anim == null) {
            println("No Animation")
            return
        }
        currentTime += delta * anim.frameInSecond * speed
        if (currentTime > anim.frameCount) {
            properties.forEach {
                it.current = 0
                it.next = 1
            }
            currentTime = 0f
        }
        properties.forEach {
            it.playNext(currentTime, revers)
            //it.field.value = it.animation.getCurrentFrame(floor(currentTime).toInt(), revers).value
        }
    }

    internal var animationIndex = -1

    suspend fun setAnimation(index: Int) {
        require(index >= 0 && index < animations.size)
        animationIndex = index
        currentAnimation = engine.resources.loadAnimation(animations[index])
    }
}