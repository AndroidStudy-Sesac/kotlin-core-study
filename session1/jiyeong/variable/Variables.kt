package variable

fun main() {
    // 1. 변수와 프로퍼티
    val name = "Jiyeong"
    // name = "New Name" // 컴파일 에러! val은 재할당 불가

    var age = 25
    age = 26 // 가능
    println("Name: $name, Age: $age")

    // 2. 커스텀 Getter/Setter
    val user = User()
    println("Initial Name: ${user.name}") // UNKNOWN (대문자로 변환됨)
    
    user.name = "Alice"
    println("Updated Name: ${user.name}") // ALICE
    
    user.name = "" // 빈 문자열은 무시됨 (Setter 로직)
    println("After Empty Set: ${user.name}") // ALICE
}

class User {
    var name: String = "Unknown"
        get() = field.uppercase() // 커스텀 Getter: 이름을 항상 대문자로 반환
        set(value) {
            if (value.isNotEmpty()) {
                field = value // field 식별자를 통해 실제 메모리 값(Backing Field)에 접근
            }
        }
}
