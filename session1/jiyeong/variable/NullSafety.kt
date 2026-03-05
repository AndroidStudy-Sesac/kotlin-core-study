package variable

fun main() {
    // 1. Nullable Type
    val text: String? = null
    println("Text: $text")

    // 2. Safe Call & Elvis Operator
    val length = text?.length ?: 0 // text가 null이면 0을 반환
    println("Length: $length")

    // 3. Not-null Assertion (사용 지양)
    // val length2 = text!!.length // text가 null이면 NPE 발생!
    
    // 4. Safe Cast
    val obj: Any = "Hello"
    val num: Int? = obj as? Int // obj가 Int가 아니면 null 반환
    println("Number: $num")
}
