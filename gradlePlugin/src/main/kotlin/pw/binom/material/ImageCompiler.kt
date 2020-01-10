package pw.binom.material

import mogot.PNGDecoder
import mogot.Resources
import mogot.SourceImage
import mogot.gl.flip2
import pw.binom.DesktopAssertTask
import pw.binom.io.wrap
import pw.binom.io.write
import pw.binom.io.writeInt
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.channels.Channels

object ImageCompiler {

    fun compile(file: File, outputFile: File) {
        if (!DesktopAssertTask.checkChanges(file, outputFile)) {
            println("$file: UP-TO-DATE")
            return
        }

        when (val fileFormat = file.extension.toLowerCase()) {
            "png" -> {
                outputFile.parentFile.mkdirs()
                outputFile.outputStream().use { output ->
                    file.inputStream().use { input ->
                        compilePng(input, output)
                    }
                }
                println("$file: compiled")
            }
            else -> throw IllegalArgumentException("Not supported file format $fileFormat")
        }
    }

    private fun compilePng(inputStream: InputStream, outputStream: OutputStream) {
        val png = PNGDecoder(inputStream)
        val color = if (png.hasAlpha())
            PNGDecoder.Format.RGBA
        else
            PNGDecoder.Format.RGB
        val buf = ByteBuffer.allocateDirect(png.width * png.height * color.numComponents)
        png.decode(buf, png.width * color.numComponents, color)

        val stream = outputStream.wrap()
        stream.write(Resources.IMAGE_MAGIC_BYTES)

        if (png.hasAlpha())
            stream.write(1)
        else
            stream.write(0)
        buf.flip2()
        stream.writeInt(png.width)
        stream.writeInt(png.height)
        stream.flush()
        val outChannel = Channels.newChannel(outputStream)
        outChannel.write(buf)
        outputStream.flush()
    }
}