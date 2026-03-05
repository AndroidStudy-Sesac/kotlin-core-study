# 1장. 변수, 타입, 그리고 안전한 초기화

## 전체 핵심 요약

- `val` / `var` → 불변성 관점에서 이해
- `const val` → 컴파일 타임 상수
- `field` / Backing Property → 캡슐화 & 상태 변경 처리
- `Any`, `Unit`, `Nothing` → 타입 계층 핵심
- `?.`, `?:`, `!!`, `let` → null 처리 핵심 문법
- `is`, `as?` + 스마트 캐스트 → 타입 검사/형변환
- `lateinit`, `lazy` → 지연 초기화 전략

---

# 1) `val` / `var` 의 차이

## 핵심

- `val` → **재할당 불가**
- `var` → **재할당 가능**

```kotlin
val name = "Kotlin"
// name = "Java" // -> 오류

var age = 20
age = 21 // 가능
```

## 왜 실무에서 `val`을 권장할까?

- 불변성(Immutability)을 유지하기 쉬움
- 멀티스레드 환경에서 더 안전함
- 버그 추적이 쉬움 (상태 변경 지점이 줄어듦)

## 실무 포인트

- 기본은 `val`
- 정말 필요한 상태 변경만 `var`
- 상태 변경이 필요하면 `var` 대신 새 객체 복사(`data class.copy()`)도 고려

## 주의: `val`은 완전 불변이 아님

`val`은 **참조 재할당 불가**일 뿐, 객체 내부 상태까지 막지는 않음

```kotlin
val list = mutableListOf(1, 2, 3)
list.add(4) // 가능 (객체 내부 변경)
```

> ✅ 정리
> 
> 
> `val` = “참조 불변”
> 
> “완전 불변”을 원하면 immutable 객체/컬렉션 설계까지 같이 가야 함
> 

---

# 2) `const val`

## 핵심

- **컴파일 시점**에 값이 결정되는 상수
- `val`은 **런타임**에 값이 결정될 수 있음
    - 예: `val time = System.currentTimeMillis()` ✅
- `const val`은 함수 호출 결과 불가
- **기본형 + String**만 가능

## 실무에서 어디에 쓰나?

- API 엔드포인트
- SharedPreferences 키값
- Intent / Bundle 키
- 고정 문자열 식별자

## 예제

```kotlin
object ApiConfig {
    const val BASE_URL = "https://api.mycompany.com/"
    const val MAX_RETRY_COUNT = 3
}
```

> ✅ 팁
> 
> 
> `object` / `companion object` 안에 상수들을 모아두는 패턴을 많이 사용
> 

---

# 3) 백킹 필드(Backing Field)와 `field`

## 지역 변수 vs 프로퍼티

- 함수 안 → **지역 변수 (local variable)**
- 클래스 안 → **프로퍼티 (property)**

```kotlin
fun main() {
    val local = "지역 변수"
}

class User {
    var name: String = "프로퍼티"
}
```

## 프로퍼티란?

프로퍼티는 단순 변수보다 확장된 개념

- 숨겨진 저장 공간(백킹 필드)
- Getter
- Setter

즉, 값을 넣고 꺼낼 때 규칙을 추가할 수 있음

---

## 왜 `field` 키워드가 필요할까?

Custom Setter 안에서 자기 자신의 이름(`name`)에 다시 할당하면

setter가 다시 호출되어 **무한 루프(재귀 호출)** 발생

```kotlin
var name: String = ""
    set(value) {
        name = value // ERROR! 또 setter를 호출해서 무한 루프 발생! StackOverflow!
    }
```

그래서 실제 저장 공간(백킹 필드)에 직접 넣기 위해 `field` 사용

- `name = value` ❌
- `field = value` ✅

---

## 실무에서는 `field`를 언제 쓰나?

Custom Setter/Getter에서 **상태 변경 시 부수 효과(Side Effect)** 가 필요할 때 자주 사용

### 예시: 값 변경 시 로그 + UI 업데이트 (Android/UI)

```kotlin
class CustomButton {
    var isActivated: Boolean = false
        set(value) {
            if (field != value) { // 값이 진짜로 변했을 때만
                field = value
                updateUiAppearance() // 화면 업데이트 등 부수 효과 실행!
                Log.d("Button", "버튼 상태가 $value 로 변경됨")
            }
        }

    private fun updateUiAppearance() { /* ... */ }
}
```

## 실무 포인트

- 값 변경 감지
- UI 갱신
- 로그 기록
- 이벤트 트리거
- 리스너 호출

---

# 4) 실무 핵심: 백킹 프로퍼티(Backing Property)

> ✅ 중요
> 
> 
> 현업에서는 `field` 자체보다 **백킹 프로퍼티 패턴**을 더 자주 봄
> 

## 왜 많이 쓰나?

- 캡슐화에 좋음
- 내부에서는 mutable
- 외부에는 read-only로 노출 가능
- ViewModel / 상태 관리에서 사실상 표준 패턴

## 핵심 패턴

- 내부용: `_userList` (변경 가능)
- 외부용: `userList` (읽기 전용)

## 예제 (필수)

```kotlin
class UserViewModel {
    // 1. 내부에서만 변경 가능한 진짜 데이터 (Backing Property)
    // 이름 앞에 언더스코어(_)를 붙이는 것이 관례입니다.
    private val _userList = mutableListOf<User>()

    // 2. 외부로 노출하는 읽기 전용 프로퍼티 (Custom Getter 사용)
    // 외부에선 _userList의 내용물을 읽을 수만 있고, 재할당은 불가능합니다.
    val userList: List<User>
        get() = _userList

    fun addUser(user: User) {
        _userList.add(user) // 내부에서는 _(언더스코어)를 이용해 자유롭게 조작
    }
}
```

## 실무 포인트 요약

1. `val` vs `var` → 기본은 `val`, 상태 변경만 `var`
2. `const val` → 고정 설정값 / 식별자
3. `field` → setter 무한루프 방지 + 상태 변경 Side Effect 처리
4. `_variable` (Backing Property) → 내부 mutable / 외부 read-only 캡슐화

---

# 5) 코틀린 타입 계층 핵심: `Any`, `Unit`, `Nothing`

---

## 5-1. `Any` — 모든 non-null 타입의 최상위 타입

```kotlin
val a: Any = 10
val b: Any = "hello"
val c: Any = true
```

### 중요한 점

- Java의 `Object`와 비슷하지만 동일하지 않음
- `Any`는 **null 포함 안 함**
- null 포함하려면 `Any?`

```kotlin
val x: Any? = null
```

### 왜 `Any`를 남발하면 안 될까? (실무 포인트)

`Any`를 많이 쓰면:

- `is` 타입 검사 증가
- `as` 캐스팅 증가
- 타입 안정성 저하
- 코드 가독성 저하

### 실무에서 자주 만나는 곳

- `equals(other: Any?)`
- `hashCode()`
- `toString()`

특히 일반 클래스에서 동등성 비교 구현 시 자주 나옴

---

## 5-2. `Unit` — 반환값은 없지만 정상 종료되는 함수의 타입

### 핵심

- Java `void`와 역할은 비슷
- 하지만 Kotlin의 `Unit`은 **실제 타입(객체, Singleton)**
- 그래서 **제네릭에 사용 가능**

### Java vs Kotlin 예제

```kotlin
// Java
interface ResultHandler<T> {
    T handle();
}
// 반환값이 없는 핸들러를 만들고 싶을 때 void를 못 씁니다.
class NoResultHandler implements ResultHandler<Void> {
    public Void handle() {
        return null; // 억지로 대문자 Void를 쓰고 null을 리턴해야 함 (지저분함)
    }
}

// Kotlin
interface ResultHandler<T> {
    fun handle(): T
}
// 반환값이 필요 없다면 T 자리에 Unit을 넣으면 끝납니다.
class NoResultHandler : ResultHandler<Unit> {
    override fun handle() {
        // 코틀린 컴파일러가 알아서 'return Unit'을 묵시적으로 처리해줌.
    }
}
```

### 안드로이드에서 자주 보이는 형태 (실무 포인트)

- `() -> Unit`
- 클릭 리스너 (`onClick`)
- 콜백 / 이벤트 핸들러

---

## 5-3. `Nothing` — 정상적으로 끝나지 않는 함수의 타입

### 자주 하는 질문

“리턴 안 하면 `Unit` 쓰면 되는 거 아닌가?”

### 차이점

- `Unit` → 정상 종료 + 반환값 없음
- `Nothing` → **절대 반환되지 않음 (Never returns)**

예:

- 예외 throw
- 무한 루프

---

### 실무에서는 언제 쓰나? (실무 포인트)

- 에러만 던지는 유틸 함수 작성 시
- `?: throw ...` 패턴과 결합할 때 매우 유용

### 예제 (필수)

```kotlin
// 예시: 무조건 예외를 던지는 함수. 반환 타입이 Nothing!
fun fail(message: String): Nothing {
    throw IllegalArgumentException(message)
}

// 실무 적용 예시 (엘비스 연산자 ?: 와 찰떡궁합)
fun processUser(user: User?) {
    // user.name이 null이면 fail()이 실행됨.
    // fail()은 Nothing을 반환하므로 컴파일러는 이 줄 이후로 name이 절대 null이 아님을 100% 확신함.
    val name: String = user?.name ?: fail("이름이 없는 유저는 처리할 수 없습니다!")

    // 만약 fail()의 반환 타입이 Unit이었다면 위 코드는 컴파일 에러가 납니다.
    // (String 자리에 Unit을 넣을 수 없으니까요)
    println(name)
}
```

---

# 6) null 처리 핵심 문법 4개 (`?.`, `?:`, `!!`, `let`)

## nullable vs non-nullable

- `String` → null 불가
- `String?` → null 가능

```kotlin
var name: String = "Kotlin"
// name = null // 오류

var nickname: String? = null // 가능
```

## 핵심 포인트

- `String`과 `String?`는 **다른 타입**
- 코틀린 null 안전성의 시작점은 **타입 분리**

---

## 6-1. `?.` 안전 호출 (Safe Call)

- null이면 그냥 null 반환
- 체이닝에 유용

```kotlin
val length: Int? = nickname?.length
```

- `nickname`이 null이면 결과는 null
- 결과 타입도 `Int?`

---

## 6-2. `?:` 엘비스 연산자 (기본값 / throw)

왼쪽이 null이면 오른쪽 값을 사용

```kotlin
val displayName = nickname ?: "Guest"
```

`throw`와도 자주 같이 씀

```kotlin
val requiredName = nickname ?: throw IllegalArgumentException("닉네임은 필수입니다.")
```

---

## 6-3. `!!` 널 단언 (주의)

개발자가 여긴 절대 null 아님을 강제로 보장하는 연산자

- 실제로 null이면 **즉시 NPE(크래시)**

## 실무 기준 (중요)

- 가능하면 **지양**
- 대체 수단 우선 사용
    - `?.`
    - `?:`
    - `requireNotNull()`

```kotlin
val safeName = requireNotNull(nickname) { "nickname must not be null" }
```

---

## 6-4. `let`

`?.let { ... }` 패턴은 **null이 아닐 때만 실행**하고 싶을 때 사용

```kotlin
nickname?.let { value ->
    println("길이: ${value.length}")
}
```

## 실무 포인트

- nullable 값을 블록 안에서 non-null처럼 다룰 수 있음
- null 체크 + 실행 로직을 깔끔하게 묶기 좋음

---

# 7) 스마트 캐스트와 타입 검사 (`is`, `!is`, `as?`)

## `is`, `!is`

- `is` → Java의 `instanceof`처럼 타입 검사
- `!is` → 특정 타입이 아님

```kotlin
fun printLength(x: Any) {
    if (x is String) {
        println(x.length) // x를 String으로 자동 인식
    }
}
```

## 스마트 캐스트란?

`is` 검사 후 컴파일러가 타입을 자동으로 좁혀주는 기능

- 수동 캐스팅 감소
- 가독성 향상
- 실수 감소

---

## 실무 예시: Sealed Class + `when` + 스마트 캐스트 (UI 상태 처리)

현업 안드로이드에서 많이 쓰는 패턴

```kotlin
// UI 상태를 정의 (로딩중, 성공, 실패)
sealed class UiState {
    object Loading : UiState()
    data class Success(val data: List<String>) : UiState()
    data class Error(val message: String) : UiState()
}

fun renderUi(state: UiState) {
    when (state) {
        is UiState.Loading -> showProgressBar()

        // 스마트 캐스트 발동! state가 Success 타입으로 변신했으므로
        // 컴파일러가 알아서 state.data에 접근하게 해줌. (형변환 불필요)
        is UiState.Success -> showList(state.data)

        // 여기서도 state가 Error로 스마트 캐스트 됨!
        is UiState.Error -> showErrorToast(state.message)
    }
}
```

## 실무 포인트

- `sealed class` + `when` + 스마트 캐스트 조합은 상태 관리에서 매우 강력함
- 분기별 타입 안정성이 좋아짐
- UI 렌더링 로직이 명확해짐

---

## `as` vs `as?`

형변환 시 안전성 차이

- `as` → 실패 시 예외 발생
- `as?` → 실패 시 `null` 반환 (안전 형변환)

### 예시

```kotlin
val x: Any = 123

val safeText: String? = x as? String
// val forceText: String = x as String // ClassCastException 가능
```

## 실무 포인트

- `as?`를 훨씬 자주 사용
- 실패 시 앱을 죽이지 않고 `null`로 후속 처리 가능

---

# 8) 지연 초기화 전략: `lateinit` vs `lazy`

## 비교표

| 구분 | `lateinit` | `lazy` |
| --- | --- | --- |
| 키워드 | `var` (변경 가능) | `val` (읽기 전용, 불변) |
| 초기화 시점 | 개발자가 원하는 타이밍에 직접 값 할당 | 변수에 **처음 접근(호출)할 때** 자동 할당 |
| 기본형(Int 등) | 사용 **불가** (String, 객체만 가능) | 사용 **가능** |
| 실무 용도 | 의존성 주입(DI), 라이프사이클에 맞춘 초기화 | 무거운 객체 생성 지연, 뷰모델(ViewModel) 초기화 |

---

## 8-1. `lateinit var`

### 의미

당장 값을 줄 수는 없지만,

**변수를 쓰기 전에는 반드시 내가 책임지고 값을 넣겠다**고 컴파일러와 약속하는 방식

### 특징

- `var`만 가능
- 의존성 주입(DI)에서 자주 사용
- 라이프사이클 기반 초기화에 적합

### 예제 (Android + Hilt)

```kotlin
// Android + Hilt 예시
@AndroidEntryPoint
class MyActivity : AppCompatActivity() {

    @Inject
    lateinit var analyticsLogger: AnalyticsLogger // Hilt가 나중에 알아서 채워줌

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 여기서 바로 Non-null처럼 쓰면된다.
        analyticsLogger.logEvent("app_started")
    }
}
```

## 실무 포인트

- DI 주입 필드
- ViewBinding
- 테스트 mock 객체 주입

---

## 8-2. `lazy`

### 의미

변수 선언 시점에는 “어떻게 만들지”만 적어두고,

**처음 접근하는 순간** 초기화 블록 실행 → 이후 재사용

### 특징

- 한 번 생성되면 계속 재사용
- 메모리를 아끼기 위해 **쓸 때 생성**
- 무거운 객체/로직의 지연 초기화에 적합

### 예제 (필수)

```kotlin
class DataProcessor {
    // 이 클래스가 생성된다고 해서 바로 DB를 로딩하지 않음. (메모리 이득)
    private val heavyDatabase: RoomDatabase by lazy {
        println("데이터베이스 초기화 중... 매우 오래 걸림!")
        Room.databaseBuilder(context, MyDatabase::class.java, "my-db").build()
    }

    fun loadData() {
        // 이 함수가 처음 불려서 heavyDatabase에 접근하는 바로 그 순간
        // 위에 있는 lazy 블록이 실행됨.
        heavyDatabase.userDao().getAllUsers()
    }
}
```

## 실무 포인트

- 무거운 객체 생성 비용 절약
- 불필요한 초기화 방지
- 초기화 시점을 코드로 명확히 표현 가능

### 참고

`lazy`는 람다(`{ }`)와 함께 쓰기 때문에 스코프 함수와 함께 자주 보임

- `apply`
- `run`
- `with`
- `let`
- `also`

---

# 최종 정리

- **`val` vs `var`**: 기본은 `val`, 상태 변경이 꼭 필요할 때만 `var`
- **`const val`**: 컴파일 타임 상수 (설정값 / 키값 / 식별자)
- **`field` (Backing Field)**: setter 무한루프 방지 + 상태 변경 Side Effect 처리
- **`_variable` (Backing Property)**: 내부 mutable / 외부 read-only 캡슐화 패턴
- **`Any` / `Unit` / `Nothing`**: 코틀린 타입 시스템 핵심 축
- **Null Safety (`?.`, `?:`, `!!`, `let`)**: 코틀린 실무 필수 문법
- **스마트 캐스트 + `is`, `as?`**: 타입 검사/형변환을 안전하고 간결하게 처리
- **`lateinit` vs `lazy`**: “나중에 초기화”의 목적/타이밍이 다르므로 구분해서 사용

---
