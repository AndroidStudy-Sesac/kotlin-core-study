# 3장. 객체지향: 상속과 인터페이스

## 전체 핵심 요약

- 코틀린 클래스는 기본이 `final` (상속 금지)
- 가시성은 `public / internal / protected / private`를 **모듈 기준**으로 이해해야 함
- 상속 시 `open`, `override`를 명시해야 함 (의도 없는 상속 방지)
- 초기화 순서(init, 프로퍼티 초기화, 부모 생성자 호출)는 실무 버그 포인트
- 인터페이스는 다중 구현 가능, 기본 구현도 가능
- 안드로이드 실무에서는 “상속 최소화 + 인터페이스/조합 우선”이 자주 권장됨

---

# 1) 클래스와 가시성 (Visibility)

## 1-1. 코틀린 클래스의 기본값: `final` + `public`

코틀린에서 클래스는 기본적으로:

- `public`
- `final` (상속 불가)

```kotlin
class UserRepository
```

위 코드는 사실상 아래 의미를 가집니다.

- 어디서든 접근 가능 (`public`)
- 다른 클래스가 상속할 수 없음 (`final`)

## 왜 기본이 `final`일까? (실무 포인트)

코틀린은 **의도하지 않은 상속**을 막아서

- 설계 안정성 확보
- 오버라이딩으로 인한 버그 감소
- 코드 추적 쉬움

> ✅ 실무 감각
> 
> 
> “상속은 기본값이 아니라, 필요할 때 명시적으로 허용”
> 

---

## 1-2. `open` — 상속/오버라이드 허용 키워드

상속 가능하게 만들려면 `open`을 붙여야 합니다.

```kotlin
open class Animal {
    open fun sound() {
        println("...")
    }
}

class Dog : Animal() {
    override fun sound() {
        println("멍멍")
    }
}
```

### 핵심

- 클래스 상속 허용: `open class`
- 메서드 오버라이드 허용: `open fun`
- 실제 재정의: `override fun`

---

## 1-3. 가시성 제어자 4개 (`public`, `internal`, `protected`, `private`)

## `public` (기본값)

- 어디서나 접근 가능

```kotlin
public class UserService
```

(보통 생략)

---

## `private`

- 선언된 범위 내부에서만 접근 가능
- 클래스 내부, 파일 내부(top-level) 등 문맥에 따라 범위가 달라짐

```kotlin
class UserManager {
    private val token = "secret"
}
```

### 실무 포인트

- 구현 디테일 숨기기
- 외부 오용 방지
- 캡슐화 기본 도구

---

## `protected`

- **클래스 내부 + 하위 클래스**에서 접근 가능
- top-level 선언에는 사용 불가

```kotlin
open class BaseActivity {
    protected fun logScreenName(name: String) {
        println("screen = $name")
    }
}

class HomeActivity : BaseActivity() {
    fun track() {
        logScreenName("home")
    }
}
```

### 실무 포인트

- 상속 구조에서 하위 클래스에만 열어줄 API에 사용
- 남용 시 상속 구조 복잡도 증가

---

## `internal` (중요: 멀티 모듈에서 자주 나옴)

- **같은 모듈(module) 내부에서만 접근 가능**
- 모듈 밖에서는 접근 불가

```kotlin
internal class NetworkInterceptor
```

## `internal`의 “정확한 범위”

여기서 모듈은 보통 다음 단위를 의미합니다.

- Gradle module (예: `:app`, `:core`, `:data`)
- 하나의 IntelliJ/Android Studio module
- 테스트 소스셋도 관련 규칙에 영향

### 왜 실무에서 중요할까? (특히 멀티 모듈)

예:

- `:data` 모듈 내부 구현체는 외부(`:app`)에 숨기고 싶음
- 공개해야 할 건 interface나 facade만 노출
- 구현 클래스는 `internal`로 감춤

```kotlin
internal class UserApiDataSource(
    private val api: UserApi
)
```

> ✅ 실무 포인트
> 
> 
> `public`으로 다 열지 말고, 모듈 내부 구현은 `internal`로 숨기면 설계가 깔끔해짐
> 

---

## 1-4. 캡슐화 (Encapsulation)

- 가시성 제어자(`private`, `protected` 등)가 존재하는 궁극적인 이유가 바로 **캡슐화**입니다.

### **개념**

객체의 상태(데이터)와 행위(메서드)를 하나로 묶고 **외부에서 함부로 건드리지 못하게 꼭꼭 숨기는 것(Information Hiding)**.

### **왜 실무에서 캡슐화에 목숨을 걸까?**

모든 변수와 메서드가 `public`으로 열려있다고 상상해 보세요.
다른 개발자(혹은 3개월 뒤의 나)가 외부 클래스에서 그 값을 맘대로 바꿔버립니다. 

나중에 버그가 터졌을 때 도대체 누가 이 값을 바꾼 거야? 하고 프로젝트 전체를 뒤져야 하는 지옥이 발생합니다.

### **✅ 실무 패턴: 백킹 프로퍼티 (Backing Property)**

안드로이드 ViewModel 등에서 숨 쉬듯이 사용하는 캡슐화의 정석 패턴입니다.

```kotlin
class UserViewModel : ViewModel() {
    // 1. 내부 로직: 나만 볼 수 있고, 나만 수정할 수 있음 (private + Mutable)
    private val _isLoading = MutableLiveData<Boolean>(false)

    // 2. 외부 공개 API: 남들은 읽기만 가능함 (public + Immutable)
    val isLoading: LiveData<Boolean> get() = _isLoading
    
    fun fetchData() {
        _isLoading.value = true // 내부에서는 자유롭게 조작
        // 데이터 로딩 로직...
    }
}
```

**✅ 실무 감각**

- **"일단 모든 변수와 메서드는 `private`으로 닫아두고 시작해라."**
- 외부(다른 클래스)에서 꼭 접근해야 하는 최소한의 기능만 `public`으로 열어주는 것이 가장 튼튼한 설계다.

---

## 1-5. 가시성 요약표

| 가시성 | 접근 범위 | 실무에서 자주 쓰는 용도 |
| --- | --- | --- |
| `public` | 어디서나 | 외부에 공개할 API |
| `internal` | 같은 모듈 내부 | 모듈 내부 구현 숨기기 |
| `protected` | 클래스 + 하위 클래스 | 상속 구조에서 하위 클래스용 API |
| `private` | 선언된 범위 내부 | 캡슐화, 구현 디테일 숨김 |

---

# 2) 초기화(Initialization) 순서

> ✅ 중요
> 
> 
> 코틀린 상속 구조에서 초기화 순서를 헷갈리면 버그가 나기 쉽습니다.
> 

---

## 2-1. 기본 초기화 요소들

클래스 초기화에 관여하는 요소:

- **주 생성자(primary constructor) 파라미터**
- **프로퍼티 초기화**
- **`init` 블록**
- **부모 클래스 생성자 호출**
- (필요 시) 보조 생성자(secondary constructor)

---

## 2-2. 클래스 내부에서의 기본 순서 (상속 없는 경우)

같은 클래스 안에서는 **코드에 적힌 순서대로** 초기화가 진행됩니다.

```kotlin
class User(name: String) {
    val upperName = name.uppercase()

    init {
        println("init block 실행")
    }

    val nameLength = name.length
}
```

대략 흐름:

1. 생성자 파라미터 전달
2. `upperName` 초기화
3. `init` 실행
4. `nameLength` 초기화

> ✅ 포인트
> 
> 
> `init`도 “초기화 순서” 안에 들어가며, 위치에 따라 실행 시점이 달라짐
> 

---

## 2-3. 상속이 있을 때 초기화 순서 (핵심)

상속이 있으면 **부모 → 자식 순서**를 기본으로 이해해야 합니다.

중요한 직관:

- 부모가 먼저 초기화되어야 자식이 안전하게 만들어질 수 있음

```kotlin
open class Parent {
    init {
        println("Parent init")
    }
}

class Child : Parent() {
    init {
        println("Child init")
    }
}
```

실행 결과:

- `Parent init`
- `Child init`

---

## 2-4. 다형성 (Polymorphism): 리모컨은 하나, 동작은 알아서

다형성은 상속과 인터페이스를 사용하는 가장 강력한 이유입니다.

개념:

- 하나의 껍데기(부모 타입 리모컨)를 조작했는데 실제 연결된 알맹이(자식 객체)에 따라 동작하는 방식이 알아서 달라지는 마법입니다.


![oop_image](https://github.com/user-attachments/assets/04d3cab7-c3d3-437b-a738-fd01d0b743c7)



객체지향 프로그래밍(OOP, Object-Oriented Programming)

- INHERITANCE (상속) 👉 Reusability (재사용성)
- ENCAPSULATION (캡슐화) 👉 Security (안전성/보안)
- POLYMORPHISM (다형성) 👉 Flexibility (유연성)


**코드 예시**

```kotlin
open class Animal {
    open fun sound() = println("...")
}

class Dog : Animal() { override fun sound() = println("멍멍") }
class Cat : Animal() { override fun sound() = println("야옹") }

fun makeSound(animal: Animal) {
    animal.sound() // 다형성 발동! 알맹이가 개면 "멍멍", 고양이시면 "야옹"
}
```

### **왜 실무에서 중요할까? (if/else 지옥 탈출)**

다형성을 안 쓰면 코드가 이렇게 됩니다.
`if (animal is Dog) animal.bark() else if (animal is Cat) animal.meow()`
동물이 100마리로 늘어나면 조건문도 100개가 됩니다. 

하지만 다형성을 쓰면 `animal.sound()` 단 한 줄로 끝납니다.

### **✅ 안드로이드 실무 연결 포인트**

- **RecyclerView 다중 뷰 타입:** 리스트 아이템이 텍스트, 이미지, 동영상 등 여러 종류일 때. 부모 타입인 `BaseItem` 리스트에 다 때려 넣고 `for`문으로 `item.bindView()`를 돌리면, 각 아이템이 알아서 자기 모양대로 화면에 그려집니다.
- **확장성 (OCP):** 새로운 기능(새로운 자식 클래스)이 추가되어도 기존에 부모 타입을 호출하던 공통 코드는 단 한 줄도 수정할 필요가 없습니다.

---

## 2-5. 상속 초기화에서 자주 하는 실수

### ❌ 부모 생성/초기화 과정에서 `open` 멤버 호출하기

부모 클래스 초기화 중에 오버라이드 가능한 멤버(`open`)를 호출하면

자식 쪽 초기화가 아직 끝나기 전이라 예상치 못한 동작이 발생할 수 있습니다.

```kotlin
open class Base {
    init {
        printValue() // 위험할 수 있음
    }

    open fun printValue() {
        println("Base")
    }
}

class Derived : Base() {
    private val value = "Derived"

    override fun printValue() {
        println(value) // 아직 초기화 전이면 문제 가능
    }
}
```

### 실무 포인트
- 생성자 / `init` / 프로퍼티 초기화 구간에서 `open` 멤버 호출 지양
- 부모 초기화 로직은 최대한 “닫힌(closed)” 상태로 설계
- 필요한 경우 별도 초기화 메서드로 분리

> ✅ **면접 포인트로도 자주 나옴**
> **“초기화 중 open member 호출 위험성”**
> 
> 좀 더 쉽게 풀어쓴 질문 ⇒ 초기화(`init`)중에 `open` 멤버를 호출하면 왜 위험한가요?
> 답변 : 부모 클래스와 자식 클래스의 초기화 순서와 다형성이 충돌하기 때문이다.
>
> <details>
> <summary><b>좀 더 정리 (여기를 클릭해서 펼쳐보세요)</b></summary>
> 
> - 객체가 생성될 때 항상 **부모 클래스가 먼저 초기화**되고, 그 다음 자식 클래스가 초기화됩니다.
> - 만약 부모의 `init` 블록에서 `open` 함수를 호출했는데 자식이 그 함수를 오버라이드했다면 다형성에 의해 **자식의 함수가 실행**됩니다.
> - 하지만 이 시점은 자식 클래스의 프로퍼티들이 아직 초기화되기 전(태어나기 전)입니다.
> - 따라서 자식의 오버라이드된 함수 내부에서 자식의 프로퍼티에 접근하려고 하면 아직 값이 세팅되지 않은 상태이기 때문에 **NullPointerException이 발생**하거나 예기치 않은 논리적 버그(쓰레기값 참조)가 발생하게 됩니다.
> - 생성자나 `init` 블록 안에서는 가급적 `final` 멤버만 호출하고 초기화 로직은 객체가 완전히 생성된 이후에 별도의 초기화 메서드나 지연 초기화(`lazy`)를 통해 안전하게 처리해야 합니다.
> </details>

---

## 2-6. 안드로이드 실무 연결 포인트

- BaseActivity / BaseFragment 패턴
- 커스텀 View 상속
- DI 주입 시점 vs init 사용 시점
- `lateinit` 값 접근 타이밍

### 실무 포인트

- `init`에서 Android lifecycle에 의존하는 작업 지양
- View/Context가 완전히 준비되기 전 접근 주의
- 생성 시점보다 `onCreate`, `onViewCreated` 등 lifecycle 콜백 활용

---

# 3) 상속 (Inheritance)

## 3-1. 코틀린 상속 기본 문법

```kotlin
open class Animal {
    open fun sound() {
        println("...")
    }
}

class Cat : Animal() {
    override fun sound() {
        println("야옹")
    }
}
```

### 핵심 키워드 정리

- `open` : 상속/오버라이드 허용
- `override` : 재정의
- 기본 클래스는 `final` 이라 상속 불가

---

## 3-2. 오버라이드 재정의 제한 (`final override`)

오버라이드한 메서드를 더 이상 하위 클래스에서 재정의 못 하게 막을 수 있습니다.

```kotlin
open class A {
    open fun foo() {}
}

open class B : A() {
    final override fun foo() {}
}

// class C : B() {
//     override fun foo() {} // 불가
// }
```

### 실무 포인트

- 프레임워크성 베이스 클래스에서 동작을 고정하고 싶을 때 유용

---

## 3-3. 부모 생성자 호출

자식 클래스는 부모 생성자를 호출해야 합니다.

```kotlin
open class Person(val name: String)

class Student(name: String, val grade: Int) : Person(name)
```

### 실무 포인트

- 상속 계층이 깊어질수록 생성자 파라미터 전달이 복잡해짐
- 그래서 실무에서는 상속보다 조합(composition)을 선호하는 경우 많음

---

## 3-4. 상속보다 조합(Composition)을 선호하는 이유 (실무 관점)

안드로이드/Kotlin 실무에서는 자주 나오는 이야기입니다.

### 상속의 장점

- 공통 기능 재사용 쉬움
- 구조가 직관적인 경우 있음

### 상속의 단점

- 결합도 증가
- 초기화 순서/override 이슈
- 변경 영향 범위 큼
- 테스트/확장 어려움

### 조합의 예 (간단)

```kotlin
class Logger {
    fun log(msg: String) = println(msg)
}

class UserService(
    private val logger: Logger
) {
    fun createUser() {
        logger.log("create user")
    }
}
```

> ✅ 실무 감각
> 
> 
> “is-a” 관계가 명확할 때만 상속, 그 외에는 조합 우선 고려
> 

---

# 4) 인터페이스 (Interface)

## 4-1. 인터페이스 기본

- 다중 구현 가능
- 추상 메서드 선언 가능
- 기본 구현(default implementation) 가능
- 상태(state)는 직접 가질 수 없지만, 프로퍼티 선언은 가능 (getter로 표현)

```kotlin
interface Clickable {
    fun onClick()

    fun showFeedback() {
        println("clicked")
    }
}
```

---

## 4-2. 인터페이스 구현

```kotlin
class Button : Clickable {
    override fun onClick() {
        println("버튼 클릭")
    }
}
```

---

## 4-3. 인터페이스의 프로퍼티

인터페이스는 프로퍼티를 선언할 수 있지만, 실제 저장소(백킹 필드)는 가질 수 없습니다.

```kotlin
interface Named {
    val name: String
    val displayName: String
        get() = "이름: $name"
}

class User(override val name: String) : Named
```

## 실무 포인트

- 공통 규약(contract) 정의에 좋음
- 구현체마다 계산 로직 커스터마이징 가능
- 저장 상태는 구현 클래스가 책임짐

---

## 4-4. 다중 인터페이스 구현과 메서드 충돌 해결

여러 인터페이스에 같은 이름의 기본 구현이 있으면, 구현 클래스에서 명시적으로 해결해야 합니다.

```kotlin
interface A {
    fun hello() {
        println("A")
    }
}

interface B {
    fun hello() {
        println("B")
    }
}

class C : A, B {
    override fun hello() {
        super<A>.hello()
        super<B>.hello()
    }
}
```

### 실무 포인트

- 다중 규약 조합 시 충돌 가능성 이해 필요
- 의도적으로 어떤 구현을 사용할지 명시 가능

---

## 4-5. 안드로이드 실무에서 인터페이스를 어디에 쓰나?

### 자주 쓰는 용도 (실무 포인트)

- 콜백 인터페이스
- 데이터 소스 추상화 (`RemoteDataSource`, `LocalDataSource`)
- Repository contract
- 이벤트 전달
- 테스트 더블(Mock/Fake) 교체 포인트

### 예시: Repository 계약 정의

```kotlin
interface UserRepository {
    fun getUserName(userId: Long): String
}
```

구현체는 여러 개 가능:

- `UserRepositoryImpl`
- `FakeUserRepository` (테스트용)

> ✅ 실무 감각
> 
> 
> 인터페이스는 “상속 대체재”라기보다 **의존성 분리 / 테스트 용이성 / 모듈 경계 설계**에 핵심
> 

---

# 5) 상속 vs 인터페이스

## 상속이 더 맞는 경우

- 명확한 **is-a 관계**
- 공통 상태/동작을 부모에서 관리
- 계층 구조가 자연스러움

예:

- `Animal` - `Dog`
- (안드로이드) 일부 Base UI 컴포넌트 패턴

## 인터페이스가 더 맞는 경우

- “무엇을 할 수 있는지(행동/규약)” 정의
- 서로 다른 클래스에 같은 기능 계약 적용
- 구현 교체 가능성 필요 (테스트 포함)

예:

- `Clickable`
- `UserRepository`
- `Logger`

> ✅ 실무에서는 보통
> 
> 
> **상속 최소화 + 인터페이스/조합 활용** 쪽으로 많이 감
> 

---

# 6) 자주 헷갈리는 포인트 정리

## 1. 왜 `open`을 계속 붙여야 하나요?

코틀린은 기본이 `final`이기 때문.

의도 없는 상속/오버라이드를 막기 위한 설계입니다.

---

## 2. `internal`은 package-private 랑 같은 건가요?

아니요. 코틀린의 `internal`은 **패키지 기준이 아니라 모듈 기준**입니다.

---

## 3. 인터페이스에도 프로퍼티가 있는데 왜 필드가 없다고 하나요?

인터페이스는 프로퍼티 “계약”만 선언할 수 있고, 실제 저장소(백킹 필드)는 구현 클래스가 가집니다.

---

## 4. 부모 `init`에서 `open` 함수 호출하면 왜 위험하죠?

자식 초기화가 끝나기 전에 오버라이드된 함수가 호출될 수 있어서 초기화되지 않은 상태를 참조할 위험이 있습니다.

---

# 7) 안드로이드 실무 연결 포인트

## 자주 만나는 패턴

- `BaseActivity`, `BaseFragment` (상속)
- `RecyclerView.Adapter`, `ViewHolder` (상속 + 인터페이스 느낌의 콜백 패턴)
- `OnClickListener` 류 계약 (인터페이스/함수형)
- Repository / DataSource 추상화 (인터페이스)
- 멀티 모듈에서 `internal`로 구현 숨기기

## 실무 포인트

- 상속은 강력하지만 결합도가 높음
- 인터페이스 + 조합이 테스트/유지보수에 유리
- `internal`을 잘 쓰면 모듈 경계가 깔끔해짐
- 초기화 순서를 모르면 상속 계층에서 버그가 생기기 쉬움

---

# 최종 정리

- 코틀린 클래스는 기본이 `final` / `public` → 상속은 `open`을 명시해야 함
- 가시성은 특히 `internal`을 **모듈 기준**으로 이해하는 게 중요
- 초기화 순서(부모→자식, `init`, 프로퍼티 초기화)는 상속 버그 핵심 포인트
- 부모 초기화 과정에서 `open` 멤버 호출은 지양
- 인터페이스는 규약/교체/테스트/모듈 경계 설계에 매우 유용
- 실무에서는 **상속 최소화 + 인터페이스/조합 우선**이 자주 권장됨
