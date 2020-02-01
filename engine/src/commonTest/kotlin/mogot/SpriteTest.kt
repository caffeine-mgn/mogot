package mogot

import mogot.math.Vector2f
import kotlin.test.Test

class SpriteTest {

    @Test
    fun test(){
        Sprite.calcPolygonTriangulation(
                listOf(
                        Vector2f(3f,1f),
                        Vector2f(2f,3f),
                        Vector2f(4f,5f),
                        Vector2f(6f,3f)
                )
        ).also {
            it.size.eq(6)
            it[0].eq(0)
            it[1].eq(1)
            it[2].eq(2)
            it[3].eq(2)
            it[4].eq(3)
            it[5].eq(0)
        }
    }

    @Test
    fun test2(){
        Sprite.calcPolygonTriangulation(
                listOf(
                        Vector2f(3f,1f),
                        Vector2f(4f,3f),
                        Vector2f(4f,5f),
                        Vector2f(6f,3f)
                )
        ).also {
            it.size.eq(6)
            it[0].eq(1)
            it[1].eq(2)
            it[2].eq(3)
            it[3].eq(3)
            it[4].eq(0)
            it[5].eq(1)
        }
    }

    @Test
    fun test3(){
        Sprite.calcPolygonTriangulation(
                listOf(
                        Vector2f(3f,1f),
                        Vector2f(1f,2f),
                        Vector2f(1f,4f),
                        Vector2f(4f,5f),
                        Vector2f(6f,3f)
                )
        ).also {
            it.size.eq(9)
            it[0].eq(0)
            it[1].eq(1)
            it[2].eq(2)
            it[3].eq(2)
            it[4].eq(3)
            it[5].eq(4)
            it[6].eq(4)
            it[7].eq(0)
            it[8].eq(2)
        }
    }
}