package mogot

import mogot.math.Vector2f
import mogot.math.Vector2fc
import mogot.math.Vector3fc
import mogot.math.Vector4fc
import mogot.math.lerp

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

    sealed class AnimatedProperty<T : Any>(val field: Field, animation: AnimationFile.Property<T>) : FrameHolder<T>(animation) {

        class Vec4(field: Field, animation: AnimationFile.Property<Vector4fc>) : AnimatedProperty<Vector4fc>(field, animation) {

            override fun playNext(frame: kotlin.Float, revers: Boolean) {
                TODO("Not yet implemented")
            }
        }

        class Vec3(field: Field, animation: AnimationFile.Property<Vector3fc>) : AnimatedProperty<Vector3fc>(field, animation) {

            override fun playNext(frame: kotlin.Float, revers: Boolean) {
                TODO("Not yet implemented")
            }
        }

        class Vec2(field: Field, animation: AnimationFile.Property<Vector2fc>) : AnimatedProperty<Vector2fc>(field, animation) {
            var currentValue = Vector2f(field.value as Vector2fc)
            var frame = animation.frames[0]
            var nextFrame = animation.frames[1]
            override fun playNext(frame: kotlin.Float, revers: Boolean) {
                lerp(frame, revers) { current, next, cof ->
                    current.lerp(next, cof, currentValue)
                }
                field.value = currentValue
            }
        }

        class Int(field: Field, animation: AnimationFile.Property<kotlin.Int>) : AnimatedProperty<kotlin.Int>(field, animation) {
            override fun playNext(frame: kotlin.Float, revers: Boolean) {
                TODO("Not yet implemented")
            }
        }

        class Float(field: Field, animation: AnimationFile.Property<kotlin.Float>) : AnimatedProperty<kotlin.Float>(field, animation) {
            override fun playNext(frame: kotlin.Float, revers: Boolean) {
                lerp(frame, revers) { current, next, cof ->
                    val v = current.lerp(next, cof)
                    field.value = v
                }
            }
        }

        class String(field: Field, animation: AnimationFile.Property<kotlin.String>) : AnimatedProperty<kotlin.String>(field, animation) {
            override fun playNext(frame: kotlin.Float, revers: Boolean) {
                TODO("Not yet implemented")
            }
        }

        class Bool(field: Field, animation: AnimationFile.Property<Boolean>) : AnimatedProperty<Boolean>(field, animation) {
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
                            Field.Type.VEC3 -> AnimatedProperty.Vec3(field, animProp as AnimationFile.Property<Vector3fc>)
                            Field.Type.VEC2 -> AnimatedProperty.Vec2(field, animProp as AnimationFile.Property<Vector2fc>)
                            Field.Type.STRING -> AnimatedProperty.String(field, animProp as AnimationFile.Property<String>)
                            Field.Type.INT -> AnimatedProperty.Int(field, animProp as AnimationFile.Property<Int>)
                            Field.Type.BOOL -> AnimatedProperty.Bool(field, animProp as AnimationFile.Property<Boolean>)
                            Field.Type.FLOAT -> AnimatedProperty.Float(field, animProp as AnimationFile.Property<Float>)
                            Field.Type.VEC4 -> AnimatedProperty.Vec4(field, animProp as AnimationFile.Property<Vector4fc>)
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

    suspend fun setAnimation(index: Int) {
        require(index >= 0 && index < animations.size)
        currentAnimation = engine.resources.loadAnimation(animations[index])
    }
}