package classobject

fun main() {
    // 1. data class
    val user1 = User("Alice", 25)
    val user2 = user1.copy(age = 26) // 이름은 그대로, 나이만 변경하여 복사
    println(user1)
    println(user2)

    val (name, age) = user1 // 구조 분해 선언
    println("$name is $age years old")
}

data class User(val name: String, val age: Int)
