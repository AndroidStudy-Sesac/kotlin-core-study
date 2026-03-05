package controlflow

fun main() {
    // 1. 범위(Range)와 진행(Progression)
    for (i in 1..10 step 2) { // 1, 3, 5, 7, 9
        print("$i ")
    }
    println()

    for (i in 10 downTo 1) { // 10, 9, ..., 1
        print("$i ")
    }
    println()

    val x = 5
    if (x in 1..10) { // x가 1 이상 10 이하인지 검사
        println("In range")
    }
}
