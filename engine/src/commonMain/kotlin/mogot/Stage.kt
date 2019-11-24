package mogot

import mogot.gl.GL

expect interface Stage
actual interface Stage{
    val gl: GL
}