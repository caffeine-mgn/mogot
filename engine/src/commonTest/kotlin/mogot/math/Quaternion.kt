package mogot.math

import kotlin.test.Test

class QuaternionTest1 {
    @Test
    fun ff(){
        val q = Quaternionf()
//        q.rotateZYX(0f, toRadians(45f),0f)
        q.setRotation(0f, toRadians(45f),0f)
        println("->$q")
    }
}