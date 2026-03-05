package classobject

fun main() {
    // 1. 확장 함수 (Extension Function)
    val text = "Kotlin"
    println("Last char: ${text.lastChar()}") // 'n'
    println("First char: ${text.firstChar()}") // 'K'

    // 2. 확장 프로퍼티 (Extension Property)
    println("Length is even: ${text.isEvenLength}") // true
}

// String 클래스에 lastChar() 함수 추가
fun String.lastChar(): Char = this.get(this.length - 1)

// String 클래스에 firstChar() 함수 추가
fun String.firstChar(): Char = this[0]

// String 클래스에 isEvenLength 프로퍼티 추가
val String.isEvenLength: Boolean
    get() = this.length % 2 == 0
