package controlflow

fun main() {
    // 1. 식(Expression)으로서의 try-catch
    val input = "123a"
    val number: Int? = try {
        Integer.parseInt(input)
    } catch (e: NumberFormatException) {
        println("Error: ${e.message}")
        null // 예외 발생 시 null 반환
    }
    
    println("Parsed number: $number")

    // 2. Nothing 타입과 예외
     fail("Critical Error") // 프로그램 종료
}

fun fail(message: String): Nothing {
    throw IllegalArgumentException(message)
}
