package mogot

expect object CurrentTime {
    fun getMillis():Long
    fun getNano():Long
}