package controlflow

fun main() {
    // 1. 식(Expression)으로서의 제어문
    val a = 10
    val b = 20
    val max = if (a > b) a else b
    println("Max: $max")

    val x = 1
    val result = when (x) {
        1 -> "One"
        2 -> "Two"
        else -> "Other"
    }
    println("Result: $result")
}
