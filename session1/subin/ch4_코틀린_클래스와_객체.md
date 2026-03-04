# 4장. 코틀린 클래스와 객체

## 전체 핵심 요약

- `data class`는 DTO/상태 모델에 최적화된 클래스 (자동 메서드 생성)
- `sealed class` / `sealed interface`는 **상태 관리 + `when` 분기 안정성**의 핵심
- `object`는 싱글톤, `companion object`는 클래스 레벨 팩토리/상수/생성 로직에 자주 사용
- `Enum`은 고정된 단순 분류, `Sealed`는 상태별 데이터가 다른 복잡한 분기에 강함
- `init`은 생성 시점 로직 처리에 유용하지만, 무거운 로직/외부 의존 작업은 지양
- `inner class`, `anonymous class`는 특정 상황에서 유용하지만 남용하면 복잡도 증가
- Access Modifier는 객체 설계/캡슐화의 핵심 (`private`, `internal`, `public`, `protected`)

---

# 1) `data class`

## 핵심

`data class`는 **데이터를 담기 위한 클래스(DTO / Model / UI State 일부)** 에 최적화된 문법입니다.

```kotlin
data class User(
    val id: Long,
    val name: String,
    val age: Int
)
```

## 왜 실무에서 많이 쓸까?

일반 클래스로 만들면 직접 구현해야 할 메서드들을 자동 생성해줍니다.

- `equals()`
- `hashCode()`
- `toString()`
- `copy()`
- `componentN()` (구조 분해 선언용)

---

## 1-1. 자동 생성 메서드와 활용

## `toString()`

디버깅할 때 보기 좋음

```kotlin
val user = User(1, "Heewon", 25)
println(user)
// User(id=1, name=Heewon, age=25)
```

### 실무 포인트

- 로그 확인
- 디버깅 중 상태 출력
- 테스트 실패 로그 가독성 향상

---

## `equals()` / `hashCode()`

내용 기준 비교 가능 (값 객체처럼 동작)

```kotlin
val u1 = User(1, "Heewon", 25)
val u2 = User(1, "Heewon", 25)

println(u1 == u2) // true (내용 비교)
```

### 실무 포인트

- 리스트 Difference(차이) 비교
- 상태 변경 감지
- 테스트 assert에서 편리함 *assert : 검증,채점

> ✅ 주의
> 
> 
> `data class`의 비교 기준은 **주 생성자(primary constructor)** 의 프로퍼티들입니다.
> 

---

## `copy()`

불변 객체 패턴과 매우 잘 맞음

```kotlin
val user = User(1, "Heewon", 25)
val updatedUser = user.copy(age = 26)
```

### 실무 포인트 (매우 중요)

- `val` 중심 상태 관리에서 상태 업데이트
- UI State 갱신
- Reducer/MVI/MVVM 상태 변경

### Android 실무 예시 (UI State 업데이트 느낌)

```kotlin
data class ProfileUiState(
    val isLoading: Boolean = false,
    val userName: String = "",
    val errorMessage: String? = null
)

val oldState = ProfileUiState(isLoading = true)
val newState = oldState.copy(
    isLoading = false,
    userName = "Heewon"
)
```

---

## `componentN()` (구조 분해 선언)

구조 분해 선언을 가능하게 해줌

```kotlin
val user = User(1, "Heewon", 25)
val (id, name, age) = user
```

### 실무 포인트

- 짧은 스코프에서 읽기 편할 때만 사용
- 너무 많은 필드 구조분해는 가독성 떨어질 수 있음

---

## 1-2. `data class` 사용 조건 / 제약

- 주 생성자에 **최소 1개 이상의 `val`/`var` 파라미터** 필요
- `abstract`, `open`, `sealed`, `inner` 불가
- 상속받을 수는 없지만(기본 `final`) 인터페이스 구현은 가능

```kotlin
interface Identifiable {
    val id: Long
}

data class Product(
    override val id: Long,
    val name: String
) : Identifiable
```

---

## 1-3. 실무에서 `data class`를 어디에 쓰나?

## 자주 쓰는 곳

- API 응답 DTO
- DB Entity (Room)
- Domain Model
- UI State Model
- 폼 입력 상태 모델

### 예시 (API 응답 DTO 느낌)

```kotlin
data class UserResponse(
    val userId: Long,
    val nickname: String,
    val profileImageUrl: String?
)
```

### 실무 포인트

- 역할이 섞이지 않게 계층별 모델 분리 권장 (`Response`, `Entity`, `UiModel` 등)
- 모든 걸 하나의 `data class`로 재사용하면 장기적으로 유지보수 어려워짐

---

# 2) `sealed class` / `sealed interface`

> ✅ 매우 중요 (실무 체감도 높음)
> 
> 
> 코틀린에서 상태 관리, 결과 타입 표현, 분기 안정성의 핵심
> 

---

## 2-1. 왜 `sealed`가 필요한가?

일반 상속 구조는 하위 타입이 어디서든 추가될 수 있습니다.

반면 `sealed`는 **하위 타입 집합을 제한**해서 컴파일러가 “가능한 경우의 수”를 정확히 알 수 있게 합니다.

### 장점

- `when` 분기에서 빠진 케이스를 컴파일 타임에 잡기 쉬움
- 상태 모델링이 명확해짐
- 유지보수 시 안전성 증가

---

## 2-2. `sealed class` 기본 예시 (UI 상태)

```kotlin
sealed class UiState {
    object Loading : UiState()
    data class Success(val data: List<String>) : UiState()
    data class Error(val message: String) : UiState()
}
```

### `when`에서 얻는 이점

```kotlin
fun render(state: UiState) {
    when (state) {
        is UiState.Loading -> showLoading()
        is UiState.Success -> showData(state.data)
        is UiState.Error -> showError(state.message)
    }
}
```

- 분기별 타입이 스마트 캐스트됨
- 상태별 데이터 접근이 안전함
- `sealed` 구조가 완전하면 `else` 없이도 작성 가능 (상황/구성에 따라)

> ✅ 실무 포인트
> 
> 
> “상태 + 상태별 데이터”가 다를 때 `Enum`보다 `Sealed`가 훨씬 강력함
> 

---

## 2-3. `sealed interface`는 언제 쓰나?

`sealed interface`는 **여러 계층/타입 그룹이 같은 규약을 공유**해야 할 때 유용합니다.

```kotlin
sealed interface AppError

data class NetworkError(val code: Int) : AppError
data class ValidationError(val field: String) : AppError
object UnknownError : AppError
```

### 실무 포인트

- 에러 타입 분류
- 도메인 이벤트 분류
- 기능별로 다른 구현체를 묶는 공통 상위 타입

---

## 2-4. Android 실무에서 자주 쓰는 패턴

### 1) 네트워크 결과 타입 래핑 (`Success / Error / Loading`)

```kotlin
sealed class Result<out T> {
    object Loading : Result<Nothing>()
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
}
```

사용 예:

```kotlin
fun handleResult(result: Result<List<String>>) {
    when (result) {
        is Result.Loading -> showLoading()
        is Result.Success -> showList(result.data)
        is Result.Error -> showError(result.message)
    }
}
```

### 2) 화면 이벤트(One-shot event) 모델링

```kotlin
sealed class ProfileEvent {
    object NavigateToLogin : ProfileEvent()
    data class ShowToast(val message: String) : ProfileEvent()
    object FinishScreen : ProfileEvent()
}
```

---

## 2-5. `sealed` 사용 시 실무 팁

- 상태가 많아지면 파일/중첩 구조 정리 필요
- UI 상태(`UiState`)와 UI 이벤트(`UiEvent`)를 섞지 않기
- `Loading / Empty / Success / Error`를 프로젝트 기준으로 일관되게 정의

---

# 3) `object` 와 `companion object`

## 3-1. `object` — 싱글톤 객체

`object` 선언은 클래스를 정의함과 동시에 **단 하나의 인스턴스**를 만듭니다.

```kotlin
object Logger {
    fun d(message: String) {
        println("[DEBUG] $message")
    }
}
```

사용:

```kotlin
Logger.d("로그 출력")
```

## 실무에서 어디에 쓰나?

- Logger / Formatter 유틸
- 앱 전역 설정 일부
- stateless helper
- object declaration 기반 singleton manager (남용 주의)

### 실무 포인트

- 전역 상태를 많이 가지는 `object`는 테스트/의존성 관리가 어려워질 수 있음
- 유틸은 편하지만, 복잡한 로직은 DI 대상 클래스로 분리하는 게 더 좋을 때 많음

---

## 3-2. `object`의 또 다른 활용: `object expression`과 구분

여기서의 `object`는 **선언(declaration)** 입니다.

- `object Logger { ... }` → 이름 있는 싱글톤 (선언)
- `object : SomeType { ... }` → 익명 객체 (표현식, 아래 anonymous class에서 다룸)

---

## 3-3. `companion object` — 클래스에 붙는 객체

`companion object`는 클래스 인스턴스 없이 접근 가능한 **클래스 레벨 멤버**를 만들 때 사용합니다.

```kotlin
class User private constructor(
    val id: Long,
    val name: String
) {
    companion object {
        fun createGuest(): User {
            return User(id = -1, name = "Guest")
        }
    }
}
```

사용:

```kotlin
val guest = User.createGuest()
```

### 왜 실무에서 많이 쓰나?

- 팩토리 메서드 제공
- 상수 정의 (`const val`)
- 생성 로직 캡슐화
- `private constructor`와 조합해 생성 경로 통제

---

## 3-4. 팩토리 메서드 패턴 (실무 자주 사용)

### 예시: 파라미터 조합/검증 후 객체 생성

```kotlin
class Nickname private constructor(
    val value: String
) {
    companion object {
        fun from(raw: String): Nickname {
            require(raw.isNotBlank()) { "닉네임은 비어 있을 수 없습니다." }
            require(raw.length in 2..10) { "닉네임은 2~10자여야 합니다." }
            return Nickname(raw.trim())
        }
    }
}
```

### 실무 포인트

- 생성 규칙을 한곳에 모음
- 잘못된 객체 생성 방지
- 도메인 모델 무결성 강화

---

## 3-5. Android 실무 예시: `Fragment.newInstance()`

`companion object`의 대표적인 안드로이드 패턴

```kotlin
class ProfileFragment : Fragment() {

    companion object {
        private const val ARG_USER_ID = "arg_user_id"

        fun newInstance(userId: Long): ProfileFragment {
            return ProfileFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_USER_ID, userId)
                }
            }
        }
    }
}
```

### 실무 포인트

- 인자 키 상수 관리
- 생성 로직 표준화
- 호출부 가독성 향상

---

# 4) Enum vs Sealed 비교

## 4-1. `enum class`가 더 맞는 경우

`enum`은 **고정된 상수 집합**을 표현할 때 좋습니다.

```kotlin
enum class UserRole {
    ADMIN, MEMBER, GUEST
}
```

### 특징

- 각 항목이 동일한 구조
- 상태별 추가 데이터가 단순할 때 적합
- 직렬화/표시/분류에 편함

### 실무 예시

- 권한 등급
- 정렬 기준
- 탭 타입
- 고정 카테고리

---

## 4-2. `sealed`가 더 맞는 경우

상태마다 **가지고 있는 데이터 구조가 다를 때** 유리합니다.

```kotlin
sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val userName: String) : LoginState()
    data class Error(val reason: String) : LoginState()
}
```

### 특징

- 상태별 데이터가 다를 수 있음
- `when` + 스마트 캐스트와 궁합 좋음
- 상태 모델링에 강력함

---

## 4-3. 선택 기준 (실무 감각)

## Enum을 선택하면 좋은 경우

- 단순한 분류값
- 상태별 추가 데이터가 거의 없음
- DB/API 매핑 값처럼 고정 상수 느낌

## Sealed를 선택하면 좋은 경우

- 상태별 payload(데이터)가 다름
- UI 상태/결과 타입/이벤트 모델링
- 분기 안정성이 중요함

> ✅ 한 줄 정리
> 
> 
> **“분류”면 Enum, “상태 + 데이터”면 Sealed** 인 경우가 많음
> 

---

# 5) `inner class`

## 5-1. nested class vs inner class (핵심)

코틀린에서 클래스 안의 클래스는 기본적으로 **nested class**이며,

기본값은 **외부 클래스 참조를 가지지 않습니다.**

```kotlin
class Outer {
    class Nested {
        fun print() {
            println("Nested")
        }
    }
}
```

반면 `inner`를 붙이면 외부 클래스 인스턴스를 참조할 수 있습니다.

```kotlin
class Outer(private val name: String) {
    inner class Inner {
        fun printOuterName() {
            println(name) // Outer의 프로퍼티 접근 가능
        }
    }
}
```

## 실무 포인트

- `inner`는 외부 객체 참조를 잡기 때문에 메모리/생명주기 주의 필요
- 안드로이드에서는 Context/Fragment/View 참조를 오래 잡으면 누수 위험 가능성도 고려

> ✅ 실무에서는 기본 nested class를 선호하고, 외부 참조가 꼭 필요할 때만 `inner` 사용
> 

---

# 6) anonymous class (익명 객체 / object expression)

## 6-1. 기본 개념

이름 없는 객체를 즉석에서 생성하는 방식입니다.

```kotlin
val listener = object {
    val tag = "temp"
    fun onEvent() {
        println("event")
    }
}
```

또는 특정 타입을 구현/상속하는 형태로 자주 사용합니다.

```kotlin
val runnable = object : Runnable {
    override fun run() {
        println("run")
    }
}
```

---

## 6-2. Android 실무에서 어디에 자주 나오나?

과거 View 시스템/리스너 기반 코드에서 특히 자주 등장했고 지금도 상황에 따라 사용합니다.

```kotlin
button.setOnClickListener(object : View.OnClickListener {
    override fun onClick(v: View?) {
        println("clicked")
    }
})
```

### 참고 (요즘 스타일)

SAM 변환 가능한 경우 람다를 더 많이 사용

```kotlin
button.setOnClickListener {
    println("clicked")
}
```

## 실무 포인트

- 익명 객체는 일회성 구현에 좋음
- 길어지면 가독성 급락 → 별도 클래스/함수/람다로 분리 고려

---

# 7) `init`

> ✅ 중요
> 
> 
> `init`은 생성 시점 로직을 넣을 수 있지만, “무엇을 넣어도 되는 블록”은 아님
> 

---

## 7-1. `init`이란?

주 생성자와 함께 동작하는 초기화 블록입니다.

```kotlin
class User(name: String) {
    val normalizedName: String

    init {
        require(name.isNotBlank()) { "이름은 비어 있을 수 없습니다." }
        normalizedName = name.trim()
    }
}
```

## 언제 쓰나? (실무 포인트)

- 생성 파라미터 검증 (`require`)
- 값 정규화/전처리
- 생성 직후 필요한 가벼운 초기화

---

## 7-2. `init`에서 지양할 것 (실무 중요)

- 무거운 I/O 작업
- 네트워크 호출
- Android lifecycle 의존 로직
- 오버라이드 가능한(`open`) 멤버 호출 (3장 연결 포인트)

### 왜?

객체 생성 타이밍이 불명확해지고, 테스트/예측 가능성이 떨어지고, 초기화 순서 문제를 만들 수 있음

---

## 7-3. Android 실무 연결 포인트

- `Fragment` / `Activity`에서 `init`보다 lifecycle 콜백(`onCreate`, `onViewCreated`) 활용
- ViewModel/도메인 모델에선 `init` 검증/초기 상태 계산은 유용
- DI 주입 이전에 `lateinit` 접근하지 않도록 주의

---

# 8) Access Modifier

> **객체 설계/캡슐화 관점으로**
> 

---

## 8-1. 핵심 요약

- `public` : 외부에 공개할 API
- `internal` : 모듈 내부 구현 숨기기 (멀티 모듈에서 중요)
- `private` : 클래스/파일 내부 구현 캡슐화
- `protected` : 상속 구조에서 하위 클래스용 API

---

## 8-2. 객체 설계에서 중요한 이유 (실무 포인트)

클래스 설계에서 Access Modifier는 단순 문법이 아니라 **의도 표현 도구**입니다.

### 예시 관점

- 외부에서 건드리면 안 되는 값 → `private`
- 모듈 내부 구현체 → `internal`
- 사용자가 호출해야 하는 API만 → `public`
- 상속 확장 지점만 → `protected`

---

## 8-3. 실무 예시: 캡슐화 + 읽기 전용 노출

```kotlin
class CartState {
    private val _items = mutableListOf<String>()
    val items: List<String>
        get() = _items

    fun addItem(item: String) {
        _items.add(item)
    }
}
```

### 실무 포인트

- 내부 상태 보호
- 외부 오용 방지
- 변경 지점을 메서드로 통제 가능

---

# 9) 추가로 같이 넣으면 좋은 내용

## 9-1. `object` + `const val` 조합 자주 씀

```kotlin
object Route {
    const val HOME = "home"
    const val PROFILE = "profile"
}
```

---

## 9-2. `sealed` + `data class` 조합 (실무 핵심 조합)

실제로 상태 관리에서 가장 많이 쓰는 조합 중 하나

```kotlin
sealed class FeedUiState {
    object Loading : FeedUiState()
    data class Success(val items: List<String>) : FeedUiState()
    data class Error(val message: String) : FeedUiState()
}
```

> ✅ 실무 포인트
> 
> 
> 4장 핵심 문법들이 실제론 “따로”보다 “조합해서” 쓰일 때 가치가 커짐
> 

---

# 10) 실무에서 자주 보는 조합 패턴

## 패턴 1) `data class` + `copy()` + `UiState`

- 화면 상태 업데이트

## 패턴 2) `sealed class` + `when`

- 상태/결과 분기 처리

## 패턴 3) `companion object` + `newInstance()` / `from()`

- 생성 로직 표준화

## 패턴 4) `private` + read-only getter

- 캡슐화 / 상태 보호

## 패턴 5) `object` utility (과용 금지)

- 간단한 stateless 유틸에 적합

---

# 11) 자주 헷갈리는 포인트 정리

## 1. `data class`는 무조건 DTO에만 써야 하나요?

아니요. DTO에 많이 쓰지만, UI 상태 모델/도메인 값 객체에도 자주 씁니다.

다만 비즈니스 로직이 과하게 들어가면 역할이 무거워질 수 있습니다.

---

## 2. `Enum`으로도 상태 관리 가능한데 왜 `Sealed`를 쓰나요?

상태마다 들고 있는 데이터가 다르면 `Sealed`가 훨씬 자연스럽고 안전합니다.

---

## 3. `object`를 많이 쓰면 싱글톤 관리가 편하지 않나요?

편할 수 있지만 전역 상태가 커지면 테스트/의존성 추적이 어려워질 수 있습니다.

복잡한 로직은 DI 가능한 클래스로 분리하는 게 좋을 때가 많습니다.

---

## 4. `init`에서 네트워크 호출하면 안 되나요?

가능은 하지만 권장하지 않습니다. 생성 시점 제어가 어려워지고, 테스트/초기화 순서/라이프사이클 이슈가 생기기 쉽습니다.

---

## 5. `inner class`는 왜 조심해야 하나요?

외부 클래스 참조를 잡기 때문에, 안드로이드에서 생명주기 긴 객체와 엮이면 메모리 누수 위험을 키울 수 있습니다.

---

# 12) 최종 정리

- `data class`는 DTO/상태 모델에 최적화되어 있고, `copy()`가 불변 상태 업데이트에 매우 유용
- `sealed class` / `sealed interface`는 상태 관리와 `when` 분기 안정성의 핵심
- `Enum`은 단순 분류, `Sealed`는 상태 + 데이터 모델링에 강함
- `object`는 싱글톤/유틸, `companion object`는 팩토리/상수/생성 로직에 유용
- `inner class`, anonymous class는 유용하지만 생명주기/가독성 관점에서 절제 필요
- `init`은 검증/정규화에 좋지만 무거운 작업이나 lifecycle 의존 작업은 지양
- Access Modifier는 문법이 아니라 **캡슐화와 모듈 경계 설계 도구**
