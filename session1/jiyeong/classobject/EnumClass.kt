package classobject

fun main() {
    // 1. Enum
    println(Color.RED.rgb)
}

enum class Color(val rgb: Int) {
    RED(0xFF0000),
    GREEN(0x00FF00),
    BLUE(0x0000FF)
}
