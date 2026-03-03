# 객체지향: 상속과 인터페이스

<details>
<summary><h2> 🔒 1. 클래스와 가시성 (Visibility Modifiers)</h2></summary>

> 코틀린은 기본적으로 **"의도치 않은 수정을 막기 위해 닫혀 있고(final), 접근에는 관대한(public)"** 성격을 가지고 있음

- **`final`**
    - 자바와 달리 코틀린의 모든 클래스와 메서드는 상속 / 재정의가 불가능한 `final` 상태
    - 상속을 허용하려면 반드시 **`open`** 키워드를 붙여야 함
- **`public`**
    - 아무것도 적지 않으면 어디서나 접근할 수 있는 `public`으로 간주함

### 🛡️ 접근 제어자(Access Modifiers) 

  | **지정자** | **접근 범위** | **설명** |
  | --- | --- | --- |
  | **`public`**  | 어디서나 | 제한 없음(기본값) |
  | **`private`** | 클래스/파일 내부 | 같은 클래스 멤버 또는 해당 파일(.kt) 내에서만 가능 |
  | **`internal`** | **같은 모듈 내부** | IntelliJ 모듈, Maven/Gradle 프로젝트 단위 내에서만 접근 가능 |
  | **`protected`** | 클래스 + 자식 클래스 | `private` 범위에 더해 상속받은 하위 클래스에서만 접근 가능 |

</details>

<details>
<summary><h2> 🏗️ 2. 초기화(Initialization) 순서</h2></summary>

> 객체가 생성될 때 실행되는 순서를 모르면 엉뚱한 값을 읽는 버그 발생 가능하므로 주의!

**실행 순서**

1. **주생성자 (Primary Constructor) 호출:** 클래스 이름 옆에 있는 녀석이 가장 먼저 실행됨
2. **`init` 블록 & 프로퍼티 초기화:** 위에서 아래로 적힌 순서대로 실행됨
3. **보조생성자 (Secondary Constructor):** 주생성자와 `init`이 다 끝난 뒤에야 가장 마지막에 실행됨
    
    > 💡 안드로이드에서는 <b>보조생성자보다 주생성자에 기본값(Default Argument)</b>을 넣는 방식을 선호함 (`class User(val name: String, val age: Int = 20)`)

    ```kotlin
    class User(val name: String) { // 1. 주생성자 실행
        val age: Int = 20          // 2-1. 프로퍼티 초기화
        
        init {                     // 2-2. 초기화 블록 실행
            println("init 실행: 이름은 $name") 
        }
    
        // 3. 보조생성자는 가장 마지막에 실행! (반드시 주생성자를 호출해야 함)
        constructor(name: String, age: Int) : this(name) { 
            println("보조생성자 실행")
        }
    }
    ```

</details>

<details>
<summary><h2> 🌳 3. 상속 (Inheritance)</h2></summary>

> 비슷한 객체 간의 공통 속성과 행위를 묶는 **"is-a"** 관계를 의미

- **단일 상속**: 클래스는 오직 하나의 부모 클래스만 가질 수 있음
- 부모 클래스는 반드시 **`open class`**
- 부모의 메서드/프로퍼티를 재정의 시 부모 쪽에 **`open`**, 자식 쪽엔 **`override`** 가 필수
    
    ```kotlin
    open class Animal(val name: String) {
        open fun sound() = println("소리를 냅니다.")
    }
    
    class Dog(name: String) : Animal(name) { // 부모 생성자 Animal(name) 호출 필수!
        override fun sound() {
            println("멍멍!")
        }
    }
    ```
    
    - 💡**주의!** 자식 클래스 생성 시, 부모 클래스의 생성자(`Animal(name)`)를 반드시 호출해 야 함

</details>

<details>
<summary><h2> 📝 4. 인터페이스 (Interface)</h2></summary>

> 객체가 지켜야 할 <b>"기능 명세(표준)"</b>를 정해주는 역할

- **다중 구현**: 클래스 상속과 달리 여러 개의 인터페이스를 동시에 구현 가능
- **기본 구현 (Default Method):** 인터페이스 안에서도 함수 몸통 `{}`을 직접 구현 가능
    (`open` 키워드가 없어도 기본적으로 열려 있어서 자유롭게 구현(`override`) 가능)
- **프로퍼티 제한**:
    - 진짜 저장 공간인 **`field`(백킹 필드)를 사용할 수는 없음**
    - 반드시 커스텀 `get()` 등으로 값을 제공하거나 자식에서 재정의해야 함

```kotlin
interface Clickable {
    val buttonName: String // 상태 저장 불가, 구현체에서 값을 줘야 함
    fun click() // 몸통이 없으면 자식 클래스에서 무조건 구현해야 함
    fun longClick() { // 몸통을 만들면 기본 동작이 됨 (자식에서 굳이 안 만들어도 됨)
        println("길게 눌렀습니다.")
    }
}

class Button : Clickable {
    override val buttonName: String = "확인버튼"
    override fun click() {
        println("$buttonName 클릭!")
    }
}
```

</details>

<details>
<summary><h2> 🎯 핵심 정리: 추상 클래스 vs 인터페이스</h2></summary>

> <b>공통된 데이터(상태)</b>를 공유해야 한다면 **추상 클래스**
> 클래스의 종류가 다르더라도 **공통된 행동 규칙**만 부여하고 싶다면 **인터페이스**

| **구분** | **추상 클래스 (Abstract Class)** | **인터페이스 (Interface)** |
| --- | --- | --- |
| **본질** | **정체성 (DNA)** | **능력 (자격증)** |
| **질문** | "너는 근본적으로 **무엇(What)**이니?" | "너는 **무엇을 할 수(Can do)** 있니?" |
| **관계** | **Is-a** (사자는 동물이다) | **Can-do** (사자는 사냥할 수 있다) |
| **상태** | 변수(데이터)를 저장할 수 있음 | 상태 저장 불가 (규칙만 전달) |
| **제한** | 하나만 상속 가능 (부모는 하나!) | 여러 개 구현 가능 (자격증은 여러 개!) |

</details>
