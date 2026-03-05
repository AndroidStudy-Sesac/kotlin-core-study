# 변수, 타입, 그리고 안전한 초기화

<details>
<summary><h2> 📦 1. Kotlin 기본 문법</h2></summary>
<br/>
  
  ### ⚖️ **`var` vs `val`**

  | **키워드** | **이름** | **의미** | **`Getter`&`Setter`** | **특징** |
  | --- | --- | --- | --- | --- |
  | **`var`** | **Variable** | 가변 (Mutable) | **`Getter`, `Setter`** | 언제든지 값을 **바꿀 수 있음** (읽기, 쓰기 가능) |
  | **`val`** | **Value** | 불변 (Immutable) | **`Getter`** | 한 번 넣으면 **바꿀 수 없음** (읽기 전용, 자바의 `final`) <br/> 함수의 파라미터는 자동으로 `val` |
  
   > 💡 절대로 안 변하는 상수를 만들고 싶을 때는 `val` 앞에 <b>`const`</b>를 붙여서 `const val` 사용
    
  <details>
  <summary>🚪 변수(Field) vs 프로퍼티(Property)</summary>
  <br/>
    
  > **프로퍼티(Property) = 변수(Field) + 접근자(Getter/Setter)**
  > - **변수 :**  메모리에 값을 저장하는 '상자'
  > - **프로퍼티 :** 그 상자에 <b>`입구(Setter)`*</b>와 <b>`출구(Getter)`</b>를 예쁘게 만들어 놓은 상태
  - **자바(Java)의 방식:** 변수를 `private`으로 숨기고, 값을 가져오거나 넣으려면 `getName()`, `setName()` 같은 함수를 일일이 만들어야 했음 (이게 다 보일러플레이트 코드)
  - **코틀린(Kotlin)의 방식:** `var name: String`이라고 딱 한 줄만 쓰면, 코틀린이 알아서 **변수 + Getter + Setter**를 묶어서 세트로 만들어줌 ⇒ 이걸 '프로퍼티'라고 함
  
    ---
    
    <details>
    <summary>💡왜 굳이 프로퍼티라고 부를까? (캡슐화)</summary>
    <br/> 
      
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
    <br/>
     
    프로퍼티 내부의 <b>진짜 데이터 저장소(Backing Field)</b>에 접근하기 위해 `field`라는 특수한 키워드를 사용
     > 만약 위 코드의 Setter 안에서 `field` 대신 `age = value`라고 써버리면 어떻게 될까?
    
    → `age`에 값을 넣기 위해 **또다시 Setter를 부르게 되고, 결국 무한 루프에 빠져 앱이 죽어버림**    
    
     </details>

     <details>
    <summary>🎯 핵심 정리</summary>
    <br/>
      
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
<summary>💻 예시 코드</summary>
<br/>
  
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
  <br/> 
  
  1. **사용하는 쪽 코드가 훨씬 깔끔해짐:** `game.setScore(-50)` 대신 `game.score = -50`이라고 쓰는 게 훨씬 직관적임 <br/>
  2. **데이터 보호 (캡슐화):** 밖에서 잘못된 데이터를 넣으려고 해도 클래스 내부에서 스스로를 보호할 수 있음 <br/>
  3. **실시간 계산:** `fullName`처럼 따로 저장할 필요 없이 그때그때 계산해서 보여주니 메모리도 아낄 수 있음

  </details>

</details>

</details>

<details>
<summary><h2>🌳 2. Kotlin 타입 계층</h2></summary>
<br/>

| **타입** | 반환 여부 | **의미 & 사용** | **자바와 비교** | **비유** |
| --- | --- | --- | --- | --- |
| **`Any`(최상위)** | **o** | **모든 타입의 최상위 부모** | `Object`와 유사 | 모든 생물의 조상 '아메바' |
| **`Unit`(싱글톤 객체)** | **o** | 반환값 없음 (성공적으로 끝남) | `void`와 유사 | “완료"라는 쪽지 한 장 |
| **`Nothing`** | **x** | 함수가 정상 종료되지 않음 | 없음 | 블랙홀 (들어가면 못 나옴) |

  <details>
  <summary>🧬 Any</summary>
  <br/>
  
  > **"코틀린 세상의 모든 객체는 Any로부터 태어났다."**
  
  ```html
  Any (최상위)
   ├── Number
   |   ├── Byte
   |   ├── Short
   │   ├── Int
   │   ├── Long
   │   ├── Float
   │   └── Double
   ├── Char
   ├── Boolean
   ├── Unit
   └── Nothing(최하위)
  ```
  
  - 특징 : 어떤 변수에 뭐가 들어올지 모를 때 사용
  - 기본 내장 함수 3가지 : `equals()`, `hashCode()`, `toString()`
    | 연산자 | 이름 | 실제 동작 | 비유 |
    | --- | ---| ---| --- |
    | `==` | 동등성 (Equality) | a.equals(b) 호출 | "두 상자 안의 내용물이 같은가?" |
    | `===` | 동일성 (Identity) | 메모리 주소 비교 | "두 상자가 실제로 같은 상자인가?" |
  
    </details>
    
    <details>
    <summary>🔢 기본 타입</summary>
    <br/>
      
  | **분류** | **타입** | **크기(bits)** | **크기(byte)** | **표현** | **설명** |
  | --- | --- | --- | --- | --- | --- |
  | **정수** | **`Byte`** | 8 | 1 | -2^7 ~ 2^7 - 1 | -128 ~ 127 |
  |  | **`Short`** | 16 | 2 | -2^{15} ~ 2^{15} - 1 | 약 -3.2만 ~ 3.2만 |
  |  | **`Int`** | 32 | 4 | -2^{31} ~ 2^{31} - 1 | 약 -21억 ~ 21억 (기본) |
  |  | **`Long`** | 64 | 8 | -2^{63} ~ 2^{63} - 1 | 아주 큰 정수 |
  | **실수** | **`Float`** | 32 | 4 | 부동소수점 | 소수점 6~7자리 정밀도 |
  |  | **`Double`** | 64 | 8 | 부동소수점 | 소수점 15~16자리 정밀도 (기본) |
  | **문자** | **`Char`** | 16 | 2 | Unicode | 유니코드 문자 (양수만 존재) |
  | **논리** | **`Boolean`** | 8 | 1 | 해당 없음 | `true` 또는 `false` |
      
  - 정수와 실수는 `Number` 클래스를 부모로 가짐
  - 다른 기본 타입으로 변경 시: `.to타입()`
  
  </details>
  
  <details>
  <summary>📝 Unit</summary>
  <br/>
  
  > "자바의 `void`와 비슷하지만, 엄연히 '객체'다."
  
  - 자바 `void`와 차이점:
    - `void`: 진짜 아무것도 없음 (타입이 아님)
    - `Unit`: 싱글톤 객체임. 그래서 제네릭 같은 곳에서도 아무런 문제 없이 쓸 수 있음
  - 특징: 코틀린 함수에서 반환 타입을 생략하면 자동으로 `Unit`이 지정됨
  - 예시
    ```Kotlin
    fun log(message: String) {
      println(message) // Unit 반환
    }
    ```
      
  </details>
  
  <details>
  <summary>🕳️ Nothing</summary>
  <br/>
    
  > "이 길의 끝은 낭떠러지야. 돌아올 수 없어."

  - 특징:
    - `예외를 던지거나(throw), 무한 루프를 돌 때 사용
    - 어떤 타입이 와야 할 자리에 `Nothing`을 넣어도 문법적으로 통과됨 (어차피 거기까지 실행이 안 될 거니까!)
    - cf. `Nothing`은 주로 '아직 구현 안 된 기능'을 표시하는 `TODO()` 함수 등에 주로 사용
  - 예시
    ```Kotlin
    fun error(message: String): Nothing {
      throw IllegalArgumentException(message)
    }

    // 만약 name이 null이면 error 함수(Nothing 반환)를 호출해라!
    // Nothing은 String의 자식이기도 해서 이 코드가 문법적으로 통과됨.
    val name: String = data.name ?: error("이름이 없어요!")
    ```
      
  </details>
</details>

<details>
<summary><h2>🛡️ 3. NullPointerException (NPE) 방지</h2></summary>
<br/>
  
  : `NullPointerException(NPE)`을 컴파일 시점에 잡음(코틀린의 최대 강점)

  | **연산자** | **이름** | **한 줄 정의** | **위험도** |
  | --- | --- | --- | --- |
  | **`?`** | **Nullable** | null 허용 | Safe |
  | **`?.`** | **Safe Call** | null이 아닐 때만 실행 | Safe |
  | **`?:`** | **Elvis Operator** | null이면 기본값을 대신 사용 | Safe |
  | **`!!`** | **NotNull  Assertion** | null이 아니라고 단언 (사용 주의!) | **Danger** |
  - 💡 ?.과 ?:를 조합해서 안전하게 코딩 권장 (!!는 사용 안하는걸 권장)

> <details>
> <summary>💡 <code>?.</code>는 주로 <code>let { }</code>과 함께 사용됨</summary>
>
> : 값이 <code>null</code>이 아닐 때만, 특정 괄호 안의 코드를 실행하고 싶을 때 사용
>
> ```kotlin
> var nickname: String? = "Gems"
> 
> // nickname이 null이 아니면, 블록 안의 it(=nickname)을 사용해라!
> nickname?.let {
>     println("내 닉네임은 $it 입니다.")
> }
> ```
>
> </details>

  <details>
  <summary>💻 예시 코드 </summary>
  <br/>
    
  ```Kotlin
    /* ? */
    var nickname: String**?** = "Gems" // null 가능!
    nickname = null // (O) 가능
    
    /** ?. */
    val length = nickname**?.**length 
    // nickname이 null이면 length도 null이 됨 (앱이 죽지 않음!)
    
    /* ?: */
    val finalLength = nickname?.length **?:** 0
    // nickname이 null이라서 length가 null이면, 0을 대신 넣어라!
    
    /* !! */
    val sureLength = nickname**!!**.length 
    // nickname이 null이면 여기서 바로 Crash! 앱 종료.
  ```
  </details>
</details>

<details>
<summary><h2>🕵️‍♂️ 4. 스마트 캐스트와 타입 검사 (is / !is, as?)</h2></summary>
<br/> 

  | **기능** | **키워드** | **역할** | **비유** |
  | --- | --- | --- | --- |
  | **타입 검사** | **`is` / `!is`** | 타입을 확인하고 `true/false` 반환 | 신분증 좀 보여주세요. |
  | **스마트 캐스트** | **`is` 후 자동** | 확인된 타입을 자동으로 변환해 줌 | 아까 확인했으니 그냥 들어가세요. |
  | **강제 형변환** | **`as`** | 무조건 특정 타입으로 바꿈 (위험) | 무조건 String이야!  |
  | **안전한 형변환** | **`as?`** | 변환 실패 시 `null` 반환 (권장) | 혹시 String이면 변신 좀 해줄래? |
  - 💡 **`is`를 통한 스마트 캐스트**를 가장 많이 사용 <br/>
     강제 변환이 필요할 땐 무조건 **`as?`와 엘비스 연산자(?:)를 조합**해서 사용 <br/>
     **`as`는 사용하지 말 것**
  
    <details><summary>🔍 타입 검사 (<code>is</code>, <code>!is</code>)</summary>
      <br/>
      
    - `is`: 특정 타입이 맞는지 확인 (맞으면 true)
    - `!is`: 특정 타입이 아닌지 확인 (아니면 true)
      ```Kotlin
      val obj: Any = "안녕"
      
      if (obj **is** String) {
        println("이건 문자열이야!")
      }
      ```
      
    </details>
    <details>
    <summary>✨ 스마트 캐스트 (Implicit Cast)</summary>
    <br/>
      
      **`is`로 타입을 한 번 확인하고 나면, 그 뒤로는 자동으로 그 타입처럼 동작**하게 함(`if (x is String)`)
      - 조건: 변수의 값이 변하지 않는다는 확신(val)이 있을 때 주로 작동
        ```Kotlin
        fun printLength(obj: Any) {
          if (obj is String) {
            // 여기서 obj는 자동으로 String으로 취급됨!
            // 따로 변환 안 해도 .length를 바로 사용 가능
            println("길이는 ${obj.length}") 
          }
        }
        ```
    
    </details>
    <details>
    <summary>🔄 형변환 (<code>as</code>, <code>as?</code>)</summary>
    <br/>
      
      - `as` (**위험**, Explicit Cast): "String으로 변해!"라고 명령하는 것으로, 만약 String이 아니면 **앱이 즉시 종료(Crash)**
      - `as?` (**안전**, Safe Cast): "너 String으로 변할 수 있니? 못하면 그냥 null을 줘."라고 정중하게 부탁
        ```Kotlin
          val x: Any = 123 // 숫자
          val s: String? = x **as?** String // String 
          // 에러 대신 s에 null이 안전하게 담김
        ```
    
    </details>
</details>

<details>
<summary><h2>⏳ 5. 지연 초기화 전략: 나중에 만들게!</h2></summary>
<br/>

  | **구분** | **`lateinit` (나중에 초기화)** | **`by lazy` (게으른 초기화)** |
  | --- | --- | --- |
  | **변수 타입** | **`var`** 전용 | **`val`** 전용 |
  | **초기화 시점** | 개발자가 원하는 시점에 수동으로! | **처음 사용할 때** 자동으로! |
  | **기본 타입** | `Int`, `Boolean` 등은 불가 (객체만 가능) | 모든 타입 가능 |
  | **특징** | 의존성 주입(Dagger, Hilt)을 받거나, <br/>  뷰(`ViewBinding`)를 초기화할 때 사용 | `ViewModel`을 가져오거나,<br/> 인텐트(`intent`)로 넘어온 데이터를 꺼낼 때, <br/>  혹은 복잡한 설정을 가진 객체를 만들 때 사용 |
  - 💡 `lateinit`은 수동, `by lazy`는 자동될 수 있으면 **`val`을 쓰는 게 안전**하므로, **`by lazy`를 먼저 고려**해보는 걸 권장
  
    <details>
    <summary>⏰ lateinit var</summary>
    <br/>
      
    주로 안드로이드의 `onCreate` 같은 생명주기 메서드 안에서 값을 넣어야 할 때 사용
    - **장점**: `null`로 선언하지 않아도 되어서, 쓸 때마다 `?`나 `!!`를 안 붙여도 됨
    - **위험**: 초기화도 안 했는데 변수를 부르면 앱이 즉시 종료됨
      ```Kotlin
      class MainActivity : AppCompatActivity() {
        // 1. 일단 선언만 함
        lateinit var name: String
      
        override fun onCreate(savedInstanceState: Bundle?) {
          super.onCreate(savedInstanceState)
          // 2. 나중에 여기서 값을 넣음!
          name = "Gemini" 
        }
      }
      ```
     </details> 
     
    > <details>
    > <summary>💡 lateinit var 안전장치 (<code>isInitialized</code>)</summary>
    >
    > : 변수가 초기화되었는지 확인하려면 <b>`::변수명.isInitialized`</b>를 사용
    >
    > ```kotlin
    > lateinit var name: String
    > 
    > fun printName() {
    >     if (::name.isInitialized) {
    >         println(name)
    >     } else {
    >         println("아직 이름이 없어요!")
    >     }
    > }
    > ```
    > </details>
    
    <details>
    <summary>🦥 by lazy</summary>
    <br/>
      
    객체를 만드는 비용이 크거나(메모리를 많이 먹거나), 굳이 미리 만들 필요가 없을 때 사용
    - 장점: 처음 딱 한 번만 실행되고, 그 이후엔 저장된 값을 그대로 사용 → 효율적임
    - 안전: 선언과 동시에 초기화 로직을 구성하기에 `lateinit`처럼 앱이 죽을 걱정이 거의 없음
      ```Kotlin
      class MyDetailActivity : AppCompatActivity() {
        // 누군가 'heavyObject'를 처음 부르는 순간 { } 안의 코드가 실행됨!
        val heavyObject: String by lazy {
            println("드디어 만드는 중...")
            "아주 무거운 객체"
        }
      }
      ```
      
    </details>
</details>
