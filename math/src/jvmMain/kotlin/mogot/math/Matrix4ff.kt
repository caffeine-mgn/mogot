package mogot.math

//actual typealias Matrix4fc = org.joml.Matrix4fc
//actual typealias Matrix4f = org.joml.Matrix4f

fun Matrix4fc.get(dest:FloatArray,offset:Int){
    dest[0 + offset] = m00
    dest[1 + offset] = m01
    dest[2 + offset] = m02
    dest[3 + offset] = m03
    dest[4 + offset] = m10
    dest[5 + offset] = m11
    dest[6 + offset] = m12
    dest[7 + offset] = m13
    dest[8 + offset] = m20
    dest[9 + offset] = m21
    dest[10 + offset] = m22
    dest[11 + offset] = m23
    dest[12 + offset] = m30
    dest[13 + offset] = m31
    dest[14 + offset] = m32
    dest[15 + offset] = m33
}