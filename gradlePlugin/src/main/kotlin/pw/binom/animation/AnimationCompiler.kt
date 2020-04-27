package pw.binom.animation

import mogot.ANIMATION_MAGIC_BYTES
import mogot.Field
import mogot.math.Vector2fc
import mogot.math.Vector3fc
import mogot.math.Vector4fc
import pw.binom.DesktopAssertTask
import pw.binom.io.*
import java.io.File
import java.io.OutputStream as JOutputStream

private const val START_OBJECT = 1.toByte()
private const val END_OBJECT = 2.toByte()
private const val PROPERTY = 3.toByte()
private const val FRAME = 4.toByte()

private class PropertyVisitorBinary(private val type: Field.Type, private val stream: OutputStream) : Animation.PropertyVisitor {
    override fun addFrame(time: Int, value: Any) {
        stream.write(FRAME)
        stream.writeInt(time)
        val r = when (type) {
            Field.Type.FLOAT -> stream.writeFloat(value as Float)
            Field.Type.INT -> stream.writeInt(value as Int)
            Field.Type.FILE,
            Field.Type.STRING -> stream.writeUTF8String(value as String)
            Field.Type.VEC2 -> {
                value as Vector2fc
                stream.writeFloat(value.x)
                stream.writeFloat(value.y)
            }
            Field.Type.VEC3 -> {
                value as Vector3fc
                stream.writeFloat(value.x)
                stream.writeFloat(value.y)
                stream.writeFloat(value.z)
            }
            Field.Type.VEC4 -> {
                value as Vector4fc
                stream.writeFloat(value.x)
                stream.writeFloat(value.y)
                stream.writeFloat(value.z)
                stream.writeFloat(value.w)
            }
            Field.Type.QUATERNION -> {
                value as Vector4fc
                stream.writeFloat(value.x)
                stream.writeFloat(value.y)
                stream.writeFloat(value.z)
                stream.writeFloat(value.w)
            }
            Field.Type.BOOL -> stream.write(if (value as Boolean) 1.toByte() else 0.toByte())
        }
    }

}

private class ObjectVisitorBinary(private val stream: OutputStream) : Animation.ObjectVisitor {
    override fun property(display: String, name: String, type: Field.Type): Animation.PropertyVisitor? {
        stream.write(PROPERTY)
        stream.writeUTF8String(display)
        stream.writeUTF8String(name)
        stream.writeInt(type.ordinal)
        return PropertyVisitorBinary(type, stream)
    }
}

private class AnimationVisitorBinary(stream: JOutputStream) : Animation.AnimationVisitor {
    private val stream = stream.wrap()
    override fun start(frameInSecond: Int, frameCount: Int) {
        stream.write(ANIMATION_MAGIC_BYTES)
        stream.writeInt(frameInSecond)
        stream.writeInt(frameCount)
    }

    override fun obj(path: String): Animation.ObjectVisitor? {
        stream.write(START_OBJECT)
        stream.writeUTF8String(path)
        return ObjectVisitorBinary(stream)
    }

    override fun end() {
        stream.write(END_OBJECT)
    }
}

object AnimationCompiler {
    @JvmStatic
    fun compile(file: File, outputStream: JOutputStream) {
        file.inputStream().bufferedReader().use {
            Animation.load(it, AnimationVisitorBinary(outputStream))
            outputStream.flush()
        }

    }

    @JvmStatic
    fun compile(file: File, outputFile: File) {
        if (!DesktopAssertTask.isFileChanged(file, outputFile)) {
            println("$file: UP-TO-DATE")
            return
        }
        outputFile.parentFile.mkdirs()
        outputFile.outputStream().use {
            compile(file, it)
            println("$file: compiled")
        }
    }
}