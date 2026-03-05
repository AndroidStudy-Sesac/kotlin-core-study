# 특수 클래스와 객체

> **데이터는 `data class`에 담고, 화면의 상태는 `sealed class`로 묶어서 `when`으로 분기 처리한다!**

<details>
<summary><h2> 🚚 1. Data Class: "데이터 운반(DTO)의 끝판왕"</h2></summary>

> 무조건 `val`로 선언하고, 값을 바꿀 땐 불변성을 유지하기 위해 `copy()` 사용을 권장함

- **필수 조건:**
    - 주생성자에 최소 1개 이상의 매개변수가 있어야 하며,
        
        반드시 `val`이나 `var`로 선언해야 함(`open`, `abstract`, `sealed` 등은 쓸 수 없음 )
        

🪄 `data class`가 자동으로 만들어주는 마법의 함수들

| **메서드명** | **역할 및 원리** | **실무 활용** |
| --- | --- | --- |
| **`equals()`** | 객체의 **'값(데이터)' 자체를 깊은 비교** <br/> (일반 클래스는 메모리 주소 비교) | 리스트에서 같은 데이터를 가진 아이템을 찾을 때 필수 |
| **`hashCode()`** | 데이터 기반으로 고유한 해시값을 생성 | `Set`, `Map`의 키로 사용할 때 완벽하게 동작함 |
| **`toString()`** | 객체를 보기 좋은 문자열로 출력 | 디버깅이나 로그(Log) 찍을 때 가독성을 높여줌 |
| **`copy()`** | 객체의 데이터를 **복사하되, 일부 값만 변경**해서 새로운 객체를 만듦 | 불변성(`val`)을 유지하면서 상태를 업데이트할 때 (Redux 패턴 등)  유용함 |
| **`componentN()`** | `val (name, age) = user` 처럼 **구조 분해(객체 분해) 할당**을 가능하게 함 | 데이터 클래스의 속성들을 개별 변수로 쪼개서 쓸 때 |

</details>

<details>
<summary><h2> 🛡️ 2. Sealed Class vs Enum Class: "상태 관리의 마스터"</h2></summary>

> 어떤 값의 종류가 딱 정해져 있을 때는 `Enum`
> <b>안드로이드 UI 상태 관리(로딩 중, 성공, 실패)</b>를 할 때는 `Enum`의 한계 때문에 무조건 <b>`Sealed Class` (봉인 클래스)</b>를 사용

🥊 Enum vs Sealed 비교

| **특징** | **Enum Class** | **Sealed Class** |
| --- | --- | --- |
| **개념** | <b>'값'</b>의 집합 (상수) | <b>'타입(클래스)'<b/>의 집합 |
| **상태/인스턴스** | 단 하나의 인스턴스만 가짐 
(값 변경 불가) | 각각 다른 상태(파라미터)를 가지는 수많은 인스턴스 생성 가능. |
| **상속** | 상속 및 서브 클래스 생성 불가. | 같은 패키지(모듈)내에서 자유롭게 상속 및 서브 클래스 생성 가능 |

<details>
<summary>💡 실무 예시: 안드로이드 네트워크 상태 관리</summary>
  
- `Sealed Class`의 진짜 강력함은 **컴파일러가 자식 클래스의 종류를 완벽하게 안다**는 점
    
    - `when` 문을 쓸 때 `else`를 생략할 수 있고, 만약 새로운 상태가 추가되면 컴파일러가 에러를 띄워 실수를 막아줌
    
      ```kotlin
      // 자식 클래스들은 반드시 같은 파일 내에 있어야 함
        sealed class HttpResult<out T> {
            data class Success<T>(val data: T) : HttpResult<T>() // 성공 (데이터를 가짐)
            data class Error(val message: String) : HttpResult<Nothing>() // 실패 (에러 메시지를 가짐)
            object Loading : HttpResult<Nothing>() // 로딩 (데이터가 필요 없으니 object 싱글턴으로 생성)
        }
      
        // when에서 else가 필요 없음! (컴파일러가 3가지 상태뿐임을 알고 있음)
        fun handleResponse(response: HttpResult<String>) {
            when (response) {
                is HttpResult.Success -> println("성공 데이터: ${response.data}")
                is HttpResult.Error -> println("에러 발생: ${response.message}")
                HttpResult.Loading -> println("로딩 중 화면 표시...") // object는 is 생략 가능
            }
        }
      ```
    
</details>

<details>
<summary>💡 참고: <code>sealed interface</code>란?</summary>
  
  - 상태가 아니라 **'행동(기능)'**을 제한하고 싶을 때는 <b>`sealed interface`</b>를 사용할 수도 있음
  - **`Sealed Class`** 와 원리는 똑같이 컴파일러가 모든 구현체를 알 수 있게 해주며, 자식들이 다중 구현(Interface)을 해야 할 때 유용하게 쓰임

</details>
</details>

<details>
<summary><h2> 🌟 3. Object & Companion Object: "싱글턴과 정적 멤버"</summary>

> 자바의 `static`을 대체하고, 싱글턴 패턴을 쉽게 만들어주는 키워드

- **`object` (싱글턴 객체):** 클래스를 정의함과 동시에 **런타임 시 자동으로 단 하나의 객체만 생성**(생성자를 가질 수 없음 )
- **`companion object` (동반 객체):**
    - 클래스 내부에서 선언하며, 외부 클래스의 이름을 통해 정적(Static) 멤버처럼 접근할 수 있음
    - **팩토리 메서드 패턴** 등에 자주 사용

```kotlin
class User private constructor(val name: String) { // 외부에서 생성 불가!
    
    // 팩토리 메서드 패턴 구현
    companion object {
        fun createAdmin(): User {
            return User("관리자")
        }
    }
}
val admin = User.createAdmin() // 정적 멤버처럼 호출
```

</details>

<details>
<summary><h2> 🪆 4. 클래스 안의 클래스 (Nested, Inner, Anonymous)</h2></summary>

| **종류** | **키워드** | **외부 클래스 접근** | **설명 및 실무 용도** |
| --- | --- | --- | --- |
| **중첩 클래스** | (없음) | ❌ 불가 | 자바의 `static` 중첩 클래스와 같음 <br/> 외부 클래스 정보가 필요 없을 때 사용 |
| **내부 클래스** | `inner` | ⭕ 가능 | 외부 클래스의 인스턴스를 통해서만 생성되며, 외부 멤버 변수를 맘대로 쓸 수 있음 |
| **익명 객체** | `object :` | - | 한 번만 쓰고 버릴 임시 객체 <br/> (예: 안드로이드 버튼 클릭 리스너 콜백 구현) |

</details>
