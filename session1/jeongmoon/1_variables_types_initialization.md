# 변수, 타입, 그리고 안전한 초기화

<details>
  <summary><h2> 📦 1. Kotlin 기본 문법</h2></summary>
  
  ### ⚖️ **`var` vs `val`**

  | **키워드** | **이름** | **의미** | **`Getter`&`Setter`** | **특징** |
  | --- | --- | --- | --- | --- |
  | **`var`** | **Variable** | 가변 (Mutable) | **`Getter`, `Setter`** | 언제든지 값을 **바꿀 수 있음** (읽기, 쓰기 가능) |
  | **`val`** | **Value** | 불변 (Immutable) | **`Getter`** | 한 번 넣으면 **바꿀 수 없음** (읽기 전용, 자바의 `final`) <br/> 함수의 파라미터는 자동으로 `val` |
  
   > 💡 절대로 안 변하는 상수를 만들고 싶을 때는 `val` 앞에 <b>`const`</b>를 붙여서 `const val` 사용
    
  <details>
    <summary>🚪 변수(Field) vs 프로퍼티(Property)</summary>
    <br>
    
  > **프로퍼티(Property) = 변수(Field) + 접근자(Getter/Setter)**
  > - **변수 :**  메모리에 값을 저장하는 '상자'
  > - **프로퍼티 :** 그 상자에 <b>`입구(Setter)`*</b>와 <b>`출구(Getter)`</b>를 예쁘게 만들어 놓은 상태
  - **자바(Java)의 방식:** 변수를 `private`으로 숨기고, 값을 가져오거나 넣으려면 `getName()`, `setName()` 같은 함수를 일일이 만들어야 했음 (이게 다 보일러플레이트 코드)
  - **코틀린(Kotlin)의 방식:** `var name: String`이라고 딱 한 줄만 쓰면, 코틀린이 알아서 **변수 + Getter + Setter**를 묶어서 세트로 만들어줌 ⇒ 이걸 '프로퍼티'라고 함
  
    ---
    
    <details>
      <summary>💡왜 굳이 프로퍼티라고 부를까? (캡슐화)</summary>
      <br> 
      
    > 그냥 변수에 바로 접근하면 편할 텐데,</br> 왜 굳이 Getter/Setter를 거칠까?
    
    → **데이터를 보호하고 제어하기 위해서**
    
    예를 들어, 어떤 사람의 `나이(age)`라는 데이터를 관리
    
    ```kotlin
    class User {
        var age: Int = 20
            set(value) {
                // 나이가 음수가 들어오면 0으로 고정해버리는 '검문소' 역할!
                field = if (value < 0) 0 else value
            }
    }
    ```
    만약 이게 그냥 변수였다면 아무나 `-100살`을 넣을 수 있었겠지만, </br>
    프로퍼티는 **Setter라는 문**이 있기 때문에 "잠깐! 나이가 왜 마이너스야? 0으로 바꿔!"라고 통제 가능해짐
    
     </details>
     
     <details>
        <summary>🚫 주의! 백킹 필드(Backing Field, <code>field</code>)를 쓰는 이유 (무한 루프 방지)</summary>
        <br>
       
    프로퍼티 내부의 <b>진짜 데이터 저장소(Backing Field)</b>에 접근하기 위해 `field`라는 특수한 키워드를 사용
     > 만약 위 코드의 Setter 안에서 `field` 대신 `age = value`라고 써버리면 어떻게 될까?
    
    → `age`에 값을 넣기 위해 **또다시 Setter를 부르게 되고, 결국 무한 루프에 빠져 앱이 죽어버림**    
    
     </details>

     <details>
        <summary>🎯 핵심 정리</summary>
        <br>
      
    > 외부에서 부르는 이름 (**프로퍼티**). 부르면 Getter/Setter가 응답 <br/>
    내부에서 데이터를 저장하는 공간 (**백킹 필드**), Getter/Setter 안에서만 쓸 수 있는 비밀 통로
    > 
    1. **변수(Field):** 데이터를 담는 순수한 공간
    2. **접근자(Getter/Setter):** 데이터를 읽거나 쓸 때 통과하는 문
    3. **프로퍼티(Property):** 변수와 접근자를 하나로 합친 개념
        
        → 아파트 현관문에 붙어 있는 **'101호'라는 호수**야. 밖에서 택배 기사님(다른 코드)들이 물건을 배달할 때 이 번호를 보고 찾아옴
        
    4. **백킹 필드(field):** 프로퍼티의 실제 값을 들고 있는 보이지 않는 저장소
        
        → 101호 문을 열고 들어가면 있는 <b>'실제 거실 공간'</b>이야. 택배 물건(데이터)이 실제로 놓이는 곳
          

  </details>

  ---

### 🛠️ 커스텀 Getter/Setter
  
  | **구분** | **호출 시점** | **주요 용도** | **비유** |
  | --- | --- | --- | --- |
  | **Custom Getter (출구 필터)** | `user.fullName` 처럼 값을 읽을 때 | 데이터 가공, 실시간 계산 | **"포장해서 내보내기"** |
  | **Custom Setter(입구 검문소)** | `game.score = 10` 처럼 값을 넣을 때 | 유효성 검사, 값 변형 <br/> (`var`에서만 가능) | **"검문소에서 걸러내기"** |

<details>
  <summary>예시</summary>
  
  - **Custom Getter 예시:** 사용자의 성과 이름을 합쳐서 전체 이름을 보여주고 싶을 때
      
      ```kotlin
      class User(val firstName: String, val lastName: String) {
          // 호출할 때마다 성과 이름을 합쳐서 리턴함
          val fullName: String
              get() = "$firstName $lastName" 
      }
      
      // 사용법
      val user = User("길동", "홍")
      println(user.fullName) // 홍 길동 (출구에서 합쳐서 나옴!)
      ```
      
  - **Custom Setter 예시:** 점수를 저장할 때 0점 미만은 무조건 0점으로 처리하고 싶을 때
      
      ```kotlin
      class Game {
          var score: Int = 0
              set(value) {
                  // value는 새로 들어온 값, field는 진짜 저장 창고!
                  field = if (value < 0) 0 else value
                  println("점수가 $field 점으로 설정되었습니다.")
              }
      }
      
      // 사용법
      val myGame = Game()
      myGame.score = -50 // 입구(Setter)에서 "어허, 0점 미만 안 돼!" 하고 걸러냄
      println(myGame.score) // 0
      ```
  </details>

  <details>
      <summary>💡왜 이렇게 번거롭게 할까?</summary>
      <br> 
  
  1. **사용하는 쪽 코드가 훨씬 깔끔해짐:** `game.setScore(-50)` 대신 `game.score = -50`이라고 쓰는 게 훨씬 직관적임 <br/>
  2. **데이터 보호 (캡슐화):** 밖에서 잘못된 데이터를 넣으려고 해도 클래스 내부에서 스스로를 보호할 수 있음 <br/>
  3. **실시간 계산:** `fullName`처럼 따로 저장할 필요 없이 그때그때 계산해서 보여주니 메모리도 아낄 수 있음

  </details>

</details>

</details>
