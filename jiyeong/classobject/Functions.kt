package classobject

fun main() {
    // 1. Default Arguments & Named Arguments
    greet("Alice") // message 생략 (기본값 사용)
    greet("Bob", "Hi") // 순서대로 전달
    greet(message = "Good Morning", name = "Charlie") // 이름 지정 (순서 무관)

    // 2. Single-Expression Function
    println("Sum: ${sum(10, 20)}")
}

fun greet(name: String, message: String = "Hello") {
    println("$message, $name!")
}

fun sum(a: Int, b: Int) = a + b
