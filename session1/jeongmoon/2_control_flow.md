# 제어 흐름

<details>
<summary><h2> 📤 1. 식(Expression)으로서의 제어문: "결과를 뱉어낸다!"</h2></summary>

> 자바와 코틀린의 가장 큰 차이점 중 하나는 `if`와 `when`이 <b>'식(Expression)'</b>으로 동작한다는 것

- **문(Statement):** 지시만 하고 끝! (예: 자바의 `if`, `switch`)
- **식(Expression):** 지시를 수행하고 **'결과값'을 반환함!** (예: 코틀린의 `if`, `when`)

  <details>
  <summary>💡 이게 왜 좋을까? (삼항 연산자가 없는 이유)</summary>
    
  - 자바에서는 값을 바로 변수에 넣기 위해 `조건 ? 참 : 거짓` (삼항 연산자)를 사용
  - **코틀린은 `if` 자체가 값을 반환하므로 삼항 연산자가 아예 필요 없음**
  
    ```kotlin
    // 자바 스타일 (문) - 변수를 먼저 만들고 값을 나중에 넣음
    var result = ""
    if (score > 80) {
        result = "합격"
    } else {
        result = "불합격"
    }
    
    // 코틀린 스타일 (식) - 변수에 if 결과를 바로 꽂아버림!
    val result = if (score > 80) "합격" else "불합격"
    ```
    
  </details>
</details>

<details>
<summary><h2> 🎯 2. Java의 switch를 완벽히 대체하는 when</h2></summary>
  
- 코틀린에는 `switch`가 없음
- 대신 `when`이 존재
  | **특징** | **설명** | **비유** |
  | --- | --- | --- |
  | **`break`가 필요 없음** | 조건이 맞으면 그거 하나만 실행하고 알아서 빠져나옴. | "볼일 끝났으면 퇴근해!" |
  | **다양한 조건 검사** | 값 비교, 타입 검사(`is`), 범위 검사(`in`) 모두 가능 | "만능 검색기" |
  | **인자 생략 가능** | `if - else if` 체인을 깔끔하게 대체 가능 | "지저분한 if문 청소기" |

  <details>
  <summary>💻 예시 코드</summary>
    
  ```kotlin
    val obj: Any = 15
    
    // 변수에 바로 값을 담는 when 식
    val description = when (obj) {
        1 -> "숫자 1이네요"
        "Hello" -> "인사말이네요"
        is Long -> "Long 타입이네요" // 타입 검사 (스마트 캐스트도 됨)
        in 10..20 -> "10에서 20 사이의 숫자네요" // 범위 검사
        else -> "뭔지 모르겠어요" // 식으로 쓸 때는 else가 필수(모든 경우를 덮어야 하므로)
    }
  ```
  
  </details> 
</details>
<details>
<summary><h2> 🔄 3. for문 활용 (3가지 필수 관용구)</h2></summary>
  
  ① 단순 아이템 순회: `for (item in list)` <br/>
  : 리스트 안에 있는 요소들만 쏙쏙 빼서 쓸 때 가장 많이 사용
  
  ```kotlin
  val names = listOf("길동", "철수", "영희")
  
  for (name in names) {
      println(name)
  }
  ```
  
  ② 인덱스와 함께 순회: `for ((index, item) in list.withIndex())` <br/>
  **: 안드로이드 UI(특히 `RecyclerView`) 다룰 때 진짜 많이 쓰는 패턴**
  
  > "몇 번째 자리에 있는 무슨 데이터"인지 알아야 할 때
  > 
  
  ```kotlin
  val names = listOf("길동", "철수", "영희")
  
  for ((index, name) in names.withIndex()) {
      println("${index}번째 학생: $name")
  }
  ```
  
  ③ 단순 반복 작업: `repeat(n) { ... }` <br/>
  : 의미 없는 인덱스 변수(`i`)를 안 만들어도 됨
  
  ```kotlin
  // "안녕!"을 3번 출력
  repeat(3) { 
      println("안녕!")
  }
  
  // 만약 몇 번째 도는 건지 알고 싶다면 it을 쓰면 돼 (0부터 시작)
  repeat(3) { 
      println("${it}번째 인사!") 
  }
  ```

</details>
<details>
<summary><h2> ✨ 4. 코틀린스럽게 코딩하기 (Idiomatic Kotlin)</h2></summary>

  <details>
  <summary>💼 for문보다 우아한 '함수형 컬렉션 연산’</summary>
    
  > 실제로 데이터 가공할 때는  `for`문보다 리스트 자체의 함수(`forEach`, `map`, `filter`)를 쓰는 걸 선호함
  > 
  
  ```kotlin
  val numbers = listOf(1, 2, 3, 4, 5)
  
  // 짝수만 걸러서(filter), 10을 곱한 뒤(map), 출력해라(forEach)
  numbers.filter { it % 2 == 0 }
         .map { it * 10 }
         .forEach { println(it) }
  ```
    
  </details>
  <details>
  <summary>🛡️ 들여쓰기를 줄이는 Early Return (가드 클로즈)</summary>
    
  > 조건이 안 맞으면 `return`으로 **빨리 함수를 종료시켜 버리는 방식을 강력하게 권장**
  > 
  
  ```kotlin
  // ❌
  fun login(user: User?) {
      if (user != null) {
          if (user.age >= 18) {
              println("로그인 성공!")
          } else {
              println("미성년자는 안 돼요.")
          }
      }
  }
  
  // ⭕ Early Return
  fun login(user: User?) {
      // 1. 입구컷 (조건 안 맞으면 바로 튕겨냄)
      if (user == null) return
      if (user.age < 18) {
          println("미성년자는 안 돼요.")
          return
      }
      
      // 2. 찐 로직 (들여쓰기 없이 깔끔하게 실행)
      println("${user.name}님 로그인 성공!")
  }
  ```

  </details>  
</details>
