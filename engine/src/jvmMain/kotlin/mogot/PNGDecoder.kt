package mogot

import pw.binom.io.InputStream
import java.io.EOFException
import java.io.IOException
import java.nio.ByteBuffer
import java.util.*
import java.util.zip.CRC32
import java.util.zip.DataFormatException
import java.util.zip.Inflater


class PNGDecoder(private val input: InputStream) {
    enum class Format(val numComponents: Int, val isHasAlpha: Boolean) {
        ALPHA(1, true),
        LUMINANCE(1, false),
        LUMINANCE_ALPHA(2, true),
        RGB(3, false),
        RGBA(4, true),
        BGRA(4, true),
        ABGR(4, true);

    }

    private val crc: CRC32
    private val buffer: ByteArray
    private var chunkLength = 0
    private var chunkType = 0
    private var chunkRemaining = 0
    var width = 0
        private set
    var height = 0
        private set
    private var bitdepth = 0
    private var colorType = 0
    private var bytesPerPixel = 0
    private lateinit var palette: ByteArray
    private var paletteA: ByteArray? = null
    private var transPixel: ByteArray? = null

    /**
     * Checks if the image has a real alpha channel.
     * This method does not check for the presence of a tRNS chunk.
     *
     * @return true if the image has an alpha channel
     * @see .hasAlpha
     */
    fun hasAlphaChannel(): Boolean {
        return colorType == COLOR_TRUEALPHA.toInt() || colorType == COLOR_GREYALPHA.toInt()
    }

    /**
     * Checks if the image has transparency information either from
     * an alpha channel or from a tRNS chunk.
     *
     * @return true if the image has transparency
     * @see .hasAlphaChannel
     * @see .overwriteTRNS
     */
    fun hasAlpha(): Boolean {
        return hasAlphaChannel() || paletteA != null || transPixel != null
    }

    val isRGB: Boolean
        get() = colorType == COLOR_TRUEALPHA.toInt() || colorType == COLOR_TRUECOLOR.toInt() || colorType == COLOR_INDEXED.toInt()

    /**
     * Overwrites the tRNS chunk entry to make a selected color transparent.
     *
     * This can only be invoked when the image has no alpha channel.
     *
     * Calling this method causes [.hasAlpha] to return true.
     *
     * @param r the red component of the color to make transparent
     * @param g the green component of the color to make transparent
     * @param b the blue component of the color to make transparent
     * @throws UnsupportedOperationException if the tRNS chunk data can't be set
     * @see .hasAlphaChannel
     */
    fun overwriteTRNS(r: Byte, g: Byte, b: Byte) {
        if (hasAlphaChannel()) {
            throw UnsupportedOperationException("image has an alpha channel")
        }
        val pal = palette
        if (pal == null) {
            transPixel = byteArrayOf(0, r, 0, g, 0, b)
        } else {
            paletteA = ByteArray(pal.size / 3)
            var i = 0
            var j = 0
            while (i < pal.size) {
                if (pal[i] != r || pal[i + 1] != g || pal[i + 2] != b) {
                    paletteA!![j] = 0xFF.toByte()
                }
                i += 3
                j++
            }
        }
    }

    /**
     * Computes the implemented format conversion for the desired format.
     *
     * @param fmt the desired format
     * @return format which  best matches the desired format
     * @throws UnsupportedOperationException if this PNG file can't be decoded
     */
    fun decideTextureFormat(fmt: Format?): Format {
        return when (colorType) {
            COLOR_TRUECOLOR -> when (fmt) {
                Format.ABGR, Format.RGBA, Format.BGRA, Format.RGB -> fmt
                else -> Format.RGB
            }
            COLOR_TRUEALPHA -> when (fmt) {
                Format.ABGR, Format.RGBA, Format.BGRA, Format.RGB -> fmt
                else -> Format.RGBA
            }
            COLOR_GREYSCALE -> when (fmt) {
                Format.LUMINANCE, Format.ALPHA -> fmt
                else -> Format.LUMINANCE
            }
            COLOR_GREYALPHA -> Format.LUMINANCE_ALPHA
            COLOR_INDEXED -> when (fmt) {
                Format.ABGR, Format.RGBA, Format.BGRA -> fmt
                else -> Format.RGBA
            }
            else -> throw UnsupportedOperationException("Not yet implemented")
        }
    }

    /**
     * Decodes the image into the specified buffer. The first line is placed at
     * the current position. After decode the buffer position is at the end of
     * the last line.
     *
     * @param buffer the buffer
     * @param stride the stride in bytes from start of a line to start of the next line, can be negative.
     * @param fmt the target format into which the image should be decoded.
     * @throws IOException if a read or data error occurred
     * @throws IllegalArgumentException if the start position of a line falls outside the buffer
     * @throws UnsupportedOperationException if the image can't be decoded into the desired format
     */
    @Throws(IOException::class)
    fun decode(buffer: ByteBuffer, stride: Int, fmt: Format?) {
        val offset = buffer.position()
        val lineSize = (width * bitdepth + 7) / 8 * bytesPerPixel
        var curLine = ByteArray(lineSize + 1)
        var prevLine = ByteArray(lineSize + 1)
        var palLine = if (bitdepth < 8) ByteArray(width + 1) else null
        val inflater = Inflater()
        try {
            for (y in 0 until height) {
                readChunkUnzip(inflater, curLine, 0, curLine.size)
                unfilter(curLine, prevLine)
                buffer.position(offset + y * stride)
                when (colorType) {
                    COLOR_TRUECOLOR -> when (fmt) {
                        Format.ABGR -> copyRGBtoABGR(buffer, curLine)
                        Format.RGBA -> copyRGBtoRGBA(buffer, curLine)
                        Format.BGRA -> copyRGBtoBGRA(buffer, curLine)
                        Format.RGB -> copy(buffer, curLine)
                        else -> throw UnsupportedOperationException("Unsupported format for this image")
                    }
                    COLOR_TRUEALPHA -> when (fmt) {
                        Format.ABGR -> copyRGBAtoABGR(buffer, curLine)
                        Format.RGBA -> copy(buffer, curLine)
                        Format.BGRA -> copyRGBAtoBGRA(buffer, curLine)
                        Format.RGB -> copyRGBAtoRGB(buffer, curLine)
                        else -> throw UnsupportedOperationException("Unsupported format for this image")
                    }
                    COLOR_GREYSCALE -> when (fmt) {
                        Format.LUMINANCE, Format.ALPHA -> copy(buffer, curLine)
                        else -> throw UnsupportedOperationException("Unsupported format for this image")
                    }
                    COLOR_GREYALPHA -> when (fmt) {
                        Format.LUMINANCE_ALPHA -> copy(buffer, curLine)
                        else -> throw UnsupportedOperationException("Unsupported format for this image")
                    }
                    COLOR_INDEXED -> {
                        when (bitdepth) {
                            8 -> palLine = curLine
                            4 -> expand4(curLine, palLine)
                            2 -> expand2(curLine, palLine)
                            1 -> expand1(curLine, palLine)
                            else -> throw UnsupportedOperationException("Unsupported bitdepth for this image")
                        }
                        when (fmt) {
                            Format.ABGR -> copyPALtoABGR(buffer, palLine)
                            Format.RGBA -> copyPALtoRGBA(buffer, palLine)
                            Format.BGRA -> copyPALtoBGRA(buffer, palLine)
                            else -> throw UnsupportedOperationException("Unsupported format for this image")
                        }
                    }
                    else -> throw UnsupportedOperationException("Not yet implemented")
                }
                val tmp = curLine
                curLine = prevLine
                prevLine = tmp
            }
        } finally {
            inflater.end()
        }
    }

    /**
     * Decodes the image into the specified buffer. The last line is placed at
     * the current position. After decode the buffer position is at the end of
     * the first line.
     *
     * @param buffer the buffer
     * @param stride the stride in bytes from start of a line to start of the next line, must be positive.
     * @param fmt the target format into which the image should be decoded.
     * @throws IOException if a read or data error occurred
     * @throws IllegalArgumentException if the start position of a line falls outside the buffer
     * @throws UnsupportedOperationException if the image can't be decoded into the desired format
     */
    @Throws(IOException::class)
    fun decodeFlipped(buffer: ByteBuffer, stride: Int, fmt: Format?) {
        require(stride > 0) { "stride" }
        val pos = buffer.position()
        val posDelta = (height - 1) * stride
        buffer.position(pos + posDelta)
        decode(buffer, -stride, fmt)
        buffer.position(buffer.position() + posDelta)
    }

    private fun copy(buffer: ByteBuffer, curLine: ByteArray) {
        buffer.put(curLine, 1, curLine.size - 1)
    }

    private fun copyRGBtoABGR(buffer: ByteBuffer, curLine: ByteArray) {
        if (transPixel != null) {
            val tr = transPixel!![1]
            val tg = transPixel!![3]
            val tb = transPixel!![5]
            var i = 1
            val n = curLine.size
            while (i < n) {
                val r = curLine[i]
                val g = curLine[i + 1]
                val b = curLine[i + 2]
                var a = 0xFF.toByte()
                if (r == tr && g == tg && b == tb) {
                    a = 0
                }
                buffer.put(a).put(b).put(g).put(r)
                i += 3
            }
        } else {
            var i = 1
            val n = curLine.size
            while (i < n) {
                buffer.put(0xFF.toByte()).put(curLine[i + 2]).put(curLine[i + 1]).put(curLine[i])
                i += 3
            }
        }
    }

    private fun copyRGBtoRGBA(buffer: ByteBuffer, curLine: ByteArray) {
        if (transPixel != null) {
            val tr = transPixel!![1]
            val tg = transPixel!![3]
            val tb = transPixel!![5]
            var i = 1
            val n = curLine.size
            while (i < n) {
                val r = curLine[i]
                val g = curLine[i + 1]
                val b = curLine[i + 2]
                var a = 0xFF.toByte()
                if (r == tr && g == tg && b == tb) {
                    a = 0
                }
                buffer.put(r).put(g).put(b).put(a)
                i += 3
            }
        } else {
            var i = 1
            val n = curLine.size
            while (i < n) {
                buffer.put(curLine[i]).put(curLine[i + 1]).put(curLine[i + 2]).put(0xFF.toByte())
                i += 3
            }
        }
    }

    private fun copyRGBtoBGRA(buffer: ByteBuffer, curLine: ByteArray) {
        if (transPixel != null) {
            val tr = transPixel!![1]
            val tg = transPixel!![3]
            val tb = transPixel!![5]
            var i = 1
            val n = curLine.size
            while (i < n) {
                val r = curLine[i]
                val g = curLine[i + 1]
                val b = curLine[i + 2]
                var a = 0xFF.toByte()
                if (r == tr && g == tg && b == tb) {
                    a = 0
                }
                buffer.put(b).put(g).put(r).put(a)
                i += 3
            }
        } else {
            var i = 1
            val n = curLine.size
            while (i < n) {
                buffer.put(curLine[i + 2]).put(curLine[i + 1]).put(curLine[i]).put(0xFF.toByte())
                i += 3
            }
        }
    }

    private fun copyRGBAtoABGR(buffer: ByteBuffer, curLine: ByteArray) {
        var i = 1
        val n = curLine.size
        while (i < n) {
            buffer.put(curLine[i + 3]).put(curLine[i + 2]).put(curLine[i + 1]).put(curLine[i])
            i += 4
        }
    }

    private fun copyRGBAtoBGRA(buffer: ByteBuffer, curLine: ByteArray) {
        var i = 1
        val n = curLine.size
        while (i < n) {
            buffer.put(curLine[i + 2]).put(curLine[i + 1]).put(curLine[i]).put(curLine[i + 3])
            i += 4
        }
    }

    private fun copyRGBAtoRGB(buffer: ByteBuffer, curLine: ByteArray) {
        var i = 1
        val n = curLine.size
        while (i < n) {
            buffer.put(curLine[i]).put(curLine[i + 1]).put(curLine[i + 2])
            i += 4
        }
    }

    private fun copyPALtoABGR(buffer: ByteBuffer, curLine: ByteArray?) {
        if (paletteA != null) {
            var i = 1
            val n = curLine!!.size
            while (i < n) {
                val idx: Int = curLine[i].toInt() and 255
                val r = palette!![idx * 3 + 0]
                val g = palette!![idx * 3 + 1]
                val b = palette!![idx * 3 + 2]
                val a = paletteA!![idx]
                buffer.put(a).put(b).put(g).put(r)
                i += 1
            }
        } else {
            var i = 1
            val n = curLine!!.size
            while (i < n) {
                val idx: Int = curLine[i].toInt() and 255
                val r = palette!![idx * 3 + 0]
                val g = palette!![idx * 3 + 1]
                val b = palette!![idx * 3 + 2]
                val a = 0xFF.toByte()
                buffer.put(a).put(b).put(g).put(r)
                i += 1
            }
        }
    }

    private fun copyPALtoRGBA(buffer: ByteBuffer, curLine: ByteArray?) {
        if (paletteA != null) {
            var i = 1
            val n = curLine!!.size
            while (i < n) {
                val idx: Int = curLine[i].toInt() and 255
                val r = palette!![idx * 3 + 0]
                val g = palette!![idx * 3 + 1]
                val b = palette!![idx * 3 + 2]
                val a = paletteA!![idx]
                buffer.put(r).put(g).put(b).put(a)
                i += 1
            }
        } else {
            var i = 1
            val n = curLine!!.size
            while (i < n) {
                val idx: Int = curLine[i].toInt() and 255
                val r = palette!![idx * 3 + 0]
                val g = palette!![idx * 3 + 1]
                val b = palette!![idx * 3 + 2]
                val a = 0xFF.toByte()
                buffer.put(r).put(g).put(b).put(a)
                i += 1
            }
        }
    }

    private fun copyPALtoBGRA(buffer: ByteBuffer, curLine: ByteArray?) {
        if (paletteA != null) {
            var i = 1
            val n = curLine!!.size
            while (i < n) {
                val idx: Int = curLine[i].toInt() and 255
                val r = palette!![idx * 3 + 0]
                val g = palette!![idx * 3 + 1]
                val b = palette!![idx * 3 + 2]
                val a = paletteA!![idx]
                buffer.put(b).put(g).put(r).put(a)
                i += 1
            }
        } else {
            var i = 1
            val n = curLine!!.size
            while (i < n) {
                val idx: Int = curLine[i].toInt() and 255
                val r = palette!![idx * 3 + 0]
                val g = palette!![idx * 3 + 1]
                val b = palette!![idx * 3 + 2]
                val a = 0xFF.toByte()
                buffer.put(b).put(g).put(r).put(a)
                i += 1
            }
        }
    }

    private fun expand4(src: ByteArray, dst: ByteArray?) {
        var i = 1
        val n = dst!!.size
        while (i < n) {
            val `val`: Int = src[1 + (i shr 1)].toInt() and 255
            when (n - i) {
                1 -> dst[i] = (`val` shr 4).toByte()
                else -> {
                    dst[i + 1] = (`val` and 15).toByte()
                    dst[i] = (`val` shr 4).toByte()
                }
            }
            i += 2
        }
    }

    private fun expand2(src: ByteArray, dst: ByteArray?) {
        var i = 1
        val n = dst!!.size
        while (i < n) {
            val `val`: Int = src[1 + (i shr 2)].toInt() and 255
            when (n - i) {
                3 -> {
                    dst[i + 2] = (`val` shr 2 and 3).toByte()
                    dst[i + 1] = (`val` shr 4 and 3).toByte()
                    dst[i] = (`val` shr 6).toByte()
                }
                2 -> {
                    dst[i + 1] = (`val` shr 4 and 3).toByte()
                    dst[i] = (`val` shr 6).toByte()
                }
                1 -> dst[i] = (`val` shr 6).toByte()
                else -> {
                    dst[i + 3] = (`val` and 3).toByte()
                    dst[i + 2] = (`val` shr 2 and 3).toByte()
                    dst[i + 1] = (`val` shr 4 and 3).toByte()
                    dst[i] = (`val` shr 6).toByte()
                }
            }
            i += 4
        }
    }

    private fun expand1(src: ByteArray, dst: ByteArray?) {
        var i = 1
        val n = dst!!.size
        while (i < n) {
            val `val`: Int = src[1 + (i shr 3)].toInt() and 255
            when (n - i) {
                7 -> {
                    dst[i + 6] = (`val` shr 1 and 1).toByte()
                    dst[i + 5] = (`val` shr 2 and 1).toByte()
                    dst[i + 4] = (`val` shr 3 and 1).toByte()
                    dst[i + 3] = (`val` shr 4 and 1).toByte()
                    dst[i + 2] = (`val` shr 5 and 1).toByte()
                    dst[i + 1] = (`val` shr 6 and 1).toByte()
                    dst[i] = (`val` shr 7).toByte()
                }
                6 -> {
                    dst[i + 5] = (`val` shr 2 and 1).toByte()
                    dst[i + 4] = (`val` shr 3 and 1).toByte()
                    dst[i + 3] = (`val` shr 4 and 1).toByte()
                    dst[i + 2] = (`val` shr 5 and 1).toByte()
                    dst[i + 1] = (`val` shr 6 and 1).toByte()
                    dst[i] = (`val` shr 7).toByte()
                }
                5 -> {
                    dst[i + 4] = (`val` shr 3 and 1).toByte()
                    dst[i + 3] = (`val` shr 4 and 1).toByte()
                    dst[i + 2] = (`val` shr 5 and 1).toByte()
                    dst[i + 1] = (`val` shr 6 and 1).toByte()
                    dst[i] = (`val` shr 7).toByte()
                }
                4 -> {
                    dst[i + 3] = (`val` shr 4 and 1).toByte()
                    dst[i + 2] = (`val` shr 5 and 1).toByte()
                    dst[i + 1] = (`val` shr 6 and 1).toByte()
                    dst[i] = (`val` shr 7).toByte()
                }
                3 -> {
                    dst[i + 2] = (`val` shr 5 and 1).toByte()
                    dst[i + 1] = (`val` shr 6 and 1).toByte()
                    dst[i] = (`val` shr 7).toByte()
                }
                2 -> {
                    dst[i + 1] = (`val` shr 6 and 1).toByte()
                    dst[i] = (`val` shr 7).toByte()
                }
                1 -> dst[i] = (`val` shr 7).toByte()
                else -> {
                    dst[i + 7] = (`val` and 1).toByte()
                    dst[i + 6] = (`val` shr 1 and 1).toByte()
                    dst[i + 5] = (`val` shr 2 and 1).toByte()
                    dst[i + 4] = (`val` shr 3 and 1).toByte()
                    dst[i + 3] = (`val` shr 4 and 1).toByte()
                    dst[i + 2] = (`val` shr 5 and 1).toByte()
                    dst[i + 1] = (`val` shr 6 and 1).toByte()
                    dst[i] = (`val` shr 7).toByte()
                }
            }
            i += 8
        }
    }

    @Throws(IOException::class)
    private fun unfilter(curLine: ByteArray, prevLine: ByteArray) {
        when (curLine[0].toInt()) {
            0 -> {
                //NOP
            }
            1 -> unfilterSub(curLine)
            2 -> unfilterUp(curLine, prevLine)
            3 -> unfilterAverage(curLine, prevLine)
            4 -> unfilterPaeth(curLine, prevLine)
            else -> throw IOException("invalide filter type in scanline: " + curLine[0])
        }
    }

    private fun unfilterSub(curLine: ByteArray) {
        val bpp = bytesPerPixel
        var i = bpp + 1
        val n = curLine.size
        while (i < n) {
            curLine[i] = ((curLine[i] + curLine[i - bpp]) and 0xFF).asByte()
            ++i
        }
    }

    private fun unfilterUp(curLine: ByteArray, prevLine: ByteArray) {
        val bpp = bytesPerPixel
        var i = 1
        val n = curLine.size
        while (i < n) {
            curLine[i] = (curLine[i] + prevLine[i]).asByte()
            ++i
        }
    }

    private fun unfilterAverage(curLine: ByteArray, prevLine: ByteArray) {
        val bpp = bytesPerPixel
        var i = 1
        while (i <= bpp) {
//            curLine[i] =(curLine[i] + ((prevLine[i].toInt() and 0xFF) ushr 1)).toByte()
            curLine[i] = (curLine[i] + (prevLine[i].toInt() and 0xFF ushr 1)).toByte()
            ++i
        }
        val n = curLine.size
        while (i < n) {
            val a = prevLine[i].toInt() and 0xFF
            val b = curLine[i - bpp].toInt() and 0xFF
            curLine[i] = (curLine[i] + ((a + b) ushr 1)).toByte()
//            curLine[i] = (curLine[i] + ((prevLine[i].toInt() and 0xFF).asByte() + (curLine[i - bpp].toInt() and 0xFF) ushr 1).asByte()).asByte()
            ++i
        }
    }

    private fun unfilterPaeth(curLine: ByteArray, prevLine: ByteArray) {
        val bpp = bytesPerPixel
        var i: Int
        i = 1
        while (i <= bpp) {
            curLine[i] = (curLine[i] + prevLine[i]).asByte()
            ++i
        }
        val n = curLine.size
        while (i < n) {
            val a: Int = curLine[i - bpp].toInt() and 255
            val b: Int = prevLine[i].toInt() and 255
            var c: Int = prevLine[i - bpp].toInt() and 255
            val p = a + b - c
            var pa = p - a
            if (pa < 0) pa = -pa
            var pb = p - b
            if (pb < 0) pb = -pb
            var pc = p - c
            if (pc < 0) pc = -pc
            if (pa <= pb && pa <= pc) c = a else if (pb <= pc) c = b
            curLine[i] = (curLine[i] + c.toByte()).toByte()
            ++i
        }
    }

    @Throws(IOException::class)
    private fun readIHDR() {
        checkChunkLength(13)
        readChunk(buffer, 0, 13)
        width = readInt(buffer, 0)
        height = readInt(buffer, 4)
        bitdepth = buffer[8].toInt() and 255
        colorType = buffer[9].toInt() and 255
        bytesPerPixel = when (colorType) {
            COLOR_GREYSCALE -> {
                if (bitdepth != 8) {
                    throw IOException("Unsupported bit depth: $bitdepth")
                }
                1
            }
            COLOR_GREYALPHA -> {
                if (bitdepth != 8) {
                    throw IOException("Unsupported bit depth: $bitdepth")
                }
                2
            }
            COLOR_TRUECOLOR -> {
                if (bitdepth != 8) {
                    throw IOException("Unsupported bit depth: $bitdepth")
                }
                3
            }
            COLOR_TRUEALPHA -> {
                if (bitdepth != 8) {
                    throw IOException("Unsupported bit depth: $bitdepth")
                }
                4
            }
            COLOR_INDEXED -> when (bitdepth) {
                8, 4, 2, 1 -> 1
                else -> throw IOException("Unsupported bit depth: $bitdepth")
            }
            else -> throw IOException("unsupported color format: $colorType")
        }
        if (buffer[10].toInt() != 0) {
            throw IOException("unsupported compression method")
        }
        if (buffer[11].toInt() != 0) {
            throw IOException("unsupported filtering method")
        }
        if (buffer[12].toInt() != 0) {
            throw IOException("unsupported interlace method")
        }
    }

    private fun readPLTE() {
        val paletteEntries = chunkLength / 3
        if (paletteEntries < 1 || paletteEntries > 256 || chunkLength % 3 != 0) {
            throw IOException("PLTE chunk has wrong length")
        }
        palette = ByteArray(paletteEntries * 3)
        readChunk(palette!!, 0, palette!!.size)
    }

    @Throws(IOException::class)
    private fun readtRNS() {
        when (colorType) {
            COLOR_GREYSCALE -> {
                checkChunkLength(2)
                transPixel = ByteArray(2)
                readChunk(transPixel!!, 0, 2)
            }
            COLOR_TRUECOLOR -> {
                checkChunkLength(6)
                transPixel = ByteArray(6)
                readChunk(transPixel!!, 0, 6)
            }
            COLOR_INDEXED -> {
                if (palette == null) {
                    throw IOException("tRNS chunk without PLTE chunk")
                }
                paletteA = ByteArray(palette!!.size / 3)
                Arrays.fill(paletteA, 0xFF.toByte())
                readChunk(paletteA!!, 0, paletteA!!.size)
            }
            else -> {
            }
        }
    }

    @Throws(IOException::class)
    private fun closeChunk() {
        if (chunkRemaining > 0) { // just skip the rest and the CRC
            skip(chunkRemaining + 4.toLong())
        } else {
            readFully(buffer, 0, 4)
            val expectedCrc = readInt(buffer, 0)
            val computedCrc = crc.value.toInt()
            if (computedCrc != expectedCrc) {
                throw IOException("Invalid CRC")
            }
        }
        chunkRemaining = 0
        chunkLength = 0
        chunkType = 0
    }

    @Throws(IOException::class)
    private fun openChunk() {
        readFully(buffer, 0, 8)
        chunkLength = readInt(buffer, 0)
        chunkType = readInt(buffer, 4)
        chunkRemaining = chunkLength
        crc.reset()
        crc.update(buffer, 4, 4) // only chunkType
    }

    @Throws(IOException::class)
    private fun openChunk(expected: Int) {
        openChunk()
        if (chunkType != expected) {
            throw IOException("Expected chunk: " + Integer.toHexString(expected))
        }
    }

    @Throws(IOException::class)
    private fun checkChunkLength(expected: Int) {
        if (chunkLength != expected) {
            throw IOException("Chunk has wrong size")
        }
    }

    @Throws(IOException::class)
    private fun readChunk(buffer: ByteArray, offset: Int, length: Int): Int {
        var length = length
        if (length > chunkRemaining) {
            length = chunkRemaining
        }
        readFully(buffer, offset, length)
        crc.update(buffer, offset, length)
        chunkRemaining -= length
        return length
    }

    @Throws(IOException::class)
    private fun refillInflater(inflater: Inflater) {
        while (chunkRemaining == 0) {
            closeChunk()
            openChunk(IDAT)
        }
        val read = readChunk(buffer, 0, buffer.size)
        inflater.setInput(buffer, 0, read)
    }

    @Throws(IOException::class)
    private fun readChunkUnzip(inflater: Inflater, buffer: ByteArray, offset: Int, length: Int) {
        var offset = offset
        var length = length
        assert(buffer != this.buffer)
        try {
            do {
                val read = inflater.inflate(buffer, offset, length)
                if (read <= 0) {
                    if (inflater.finished()) {
                        throw EOFException()
                    }
                    if (inflater.needsInput()) {
                        refillInflater(inflater)
                    } else {
                        throw IOException("Can't inflate $length bytes")
                    }
                } else {
                    offset += read
                    length -= read
                }
            } while (length > 0)
        } catch (ex: DataFormatException) {
            throw (IOException("inflate error").initCause(ex) as IOException)
        }
    }

    @Throws(IOException::class)
    private fun readFully(buffer: ByteArray, offset: Int, length: Int) {
        var offset = offset
        var length = length
        do {
            val read = input.read(buffer, offset, length)
            if (read < 0) {
                throw EOFException()
            }
            offset += read
            length -= read
        } while (length > 0)
    }

    private fun readInt(buffer: ByteArray, offset: Int): Int {
        return buffer[offset].toInt() shl 24 or
                (buffer[offset + 1].toInt() and 255 shl 16) or
                (buffer[offset + 2].toInt() and 255 shl 8) or
                (buffer[offset + 3].toInt() and 255)
    }

    @Throws(IOException::class)
    private fun skip(amount: Long) {
        var amount = amount
        while (amount > 0) {
            val skipped = input.skip(amount)
            if (skipped < 0) {
                throw EOFException()
            }
            amount -= skipped
        }
    }

    init {
        crc = CRC32()
        buffer = ByteArray(4096)
        readFully(buffer, 0, SIGNATURE.size)
        if (!checkSignature(buffer)) {
            throw IOException("Not a valid PNG file")
        }
        openChunk(IHDR)
        readIHDR()
        closeChunk()
        searchIDAT@ while (true) {
            openChunk()
            when (chunkType) {
                IDAT -> break@searchIDAT
                PLTE -> readPLTE()
                tRNS -> readtRNS()
            }
            closeChunk()
        }
        if (colorType == COLOR_INDEXED.toInt() && palette == null) {
            throw IOException("Missing PLTE chunk")
        }
    }
}

private val SIGNATURE = byteArrayOf(137.toByte(), 80, 78, 71, 13, 10, 26, 10)
private const val IHDR = 0x49484452
private const val PLTE = 0x504C5445
private const val tRNS = 0x74524E53
private const val IDAT = 0x49444154
private const val IEND = 0x49454E44
private const val COLOR_GREYSCALE = 0
private const val COLOR_TRUECOLOR = 2
private const val COLOR_INDEXED = 3
private const val COLOR_GREYALPHA = 4
private const val COLOR_TRUEALPHA = 6
private fun checkSignature(buffer: ByteArray): Boolean {
    for (i in SIGNATURE.indices) {
        if (buffer[i] != SIGNATURE[i]) {
            return false
        }
    }
    return true
}

fun Int.asByte(): Byte {
    return this.toByte()
    return (this and 0xFF).toByte()
}