package mogot

import kotlin.test.Test
import kotlin.test.assertNull

class AnimationFileTest {

    @Test
    fun test() {
        val VAL1 = 1
        val VAL2 = 2
        val FRAME1 = 5
        val FRAME2 = 15
        val obj = AnimationFile.Object("")
        val file = AnimationFile(60, 60, mutableListOf(obj))
        val property = AnimationFile.Property<Int>(file, Field.Type.INT, "")
        property.frames.add(AnimationFile.Frame(FRAME1, VAL1))
        property.frames.add(AnimationFile.Frame(FRAME2, VAL2))

        property.getCurrentFrame(FRAME1 - 1, false).value.eq(VAL1)
        property.getCurrentFrame(FRAME1, false).value.eq(VAL1)

        property.getCurrentFrame(FRAME2, false).value.eq(VAL2)
        property.getNextFrame(FRAME1, false)!!.value.eq(VAL2)
        property.getNextFrame(FRAME2 - 1, false)!!.value.eq(VAL2)
        assertNull(property.getNextFrame(FRAME2 + 1, false))
    }

    @Test
    fun lerpTest() {
        val obj = AnimationFile.Object("")
        val file = AnimationFile(15, 60, mutableListOf(obj))
        val prop = AnimationFile.Property<Float>(file, Field.Type.FLOAT, "")
        val f1 = AnimationFile.Frame(0, 0f)
        val f2 = AnimationFile.Frame(5, 0f)
        val f3 = AnimationFile.Frame(10, 0f)
        prop.frames.add(f1)
        prop.frames.add(f2)
        prop.frames.add(f3)
        val holder = FrameHolder(prop)

        (0..4).forEach {
            holder.frames(it.toFloat(), false).apply {
                first.eq(f1)
                second.eq(f2)
            }
        }
        (5..9).forEach {
            holder.frames(it.toFloat(), false).apply {
                first.eq(f2)
                second.eq(f3)
            }
        }

        (10..15).forEach {
            holder.frames(it.toFloat(), false).apply {
                first.eq(f3)
                second.eq(f1)
            }
        }

    }
}