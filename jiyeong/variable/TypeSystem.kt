package variable

fun main() {
    // 1. Any
    val any: Any = "Hello"
    println("Any: $any")

    // 2. Unit
    val unit: Unit = printHello()
    println("Unit: $unit")

    // 3. Nothing
//     fail("Error") // 예외 발생

    // 4. Smart Cast
    checkType("Hello")
    checkType(123)
}

fun printHello(): Unit {
    println("Hello")
}

fun fail(message: String): Nothing {
    throw IllegalArgumentException(message)
}

fun checkType(obj: Any) {
    if (obj is String) {
        // 이 블록 안에서는 obj가 자동으로 String으로 캐스팅됨
        println("String length: ${obj.length}")
    }
    
    val num: Int? = obj as? Int // obj가 Int가 아니면 null 반환
    println("Number: $num")
}
