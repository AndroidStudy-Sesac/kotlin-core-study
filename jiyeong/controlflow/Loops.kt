package controlflow

fun main() {
    // 1. 실무 압축 for문 활용
    val items = listOf("Apple", "Banana", "Cherry")

    // 단순 아이템 순회
    for (item in items) {
        println(item)
    }

    // 인덱스와 함께 순회
    for ((index, item) in items.withIndex()) {
        println("Index: $index, Item: $item")
    }

    // 단순 반복 작업
    repeat(5) { count ->
        println("Hello $count") // 0, 1, 2, 3, 4 출력
    }
}
