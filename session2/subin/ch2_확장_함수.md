# 2장. 확장 함수 (Extension Functions)

## 전체 핵심 요약

- 확장 함수는 기존 클래스에 “멤버를 추가한 것처럼” 보이지만, 실제로는 **정적(static) 함수로 컴파일**됨
- 그래서 **오버라이드(다형성) 불가** → “멤버처럼 보이지만 멤버가 아님”
- 확장 함수는 **호출 시점의 변수 타입(정적 타입)** 으로 결정됨 (static dispatch)
- Android에서 `View`, `Context`, `Fragment`, `Intent`, `TextView`, `RecyclerView` 등에 확장 함수를 엄청 많이 사용
- 확장 함수는 편하지만 남용하면 “전역 유틸 난립”이 되므로 모듈/패키지 경계를 잘 잡는 게 중요

---

# 1) 확장 함수의 원리: “멤버 추가처럼 보이게 하는 문법”

## 핵심

확장 함수는 “기존 클래스에 함수 추가”처럼 보이지만 실제로는 아래 형태입니다.

```kotlin
fun String.lastChar(): Char = this[this.length - 1]

val c = "Kotlin".lastChar()
```

- `String`을 수정하는 게 아님
- `String`에 진짜 멤버가 생기는 것도 아님
- “그렇게 보이게 호출 문법을 제공”하는 것

---

## 언제 확장 함수를 쓰나? (실무 포인트)

- 반복되는 유틸을 깔끔한 API처럼 만들고 싶을 때
- 도메인/UI에서 “읽히는 코드”를 만들고 싶을 때
- Kotlin의 장점인 “DSL스러운 코드”를 살리고 싶을 때

---

# 2) 내부 동작: 자바로 디컴파일하면 정적(static) 함수가 된다

## 핵심

확장 함수는 컴파일 시 “정적 함수 + 첫 번째 파라미터가 receiver” 형태로 바뀝니다.

예를 들어:

```kotlin
fun View.visible() { visibility = View.VISIBLE }
```

개념적으로는 이런 식으로 변환됩니다.

```kotlin
// (개념) Java 스타일
public static void visible(View receiver) { receiver.setVisibility(View.VISIBLE); }
```

> ✅ 결론
> 
> 
> 확장 함수는 **정적 디스패치(static dispatch)** 기반이라
> 
> **오버라이딩(동적 디스패치)** 이 일어나지 않습니다.
> 

---

# 3) 오버라이딩 가능 여부 & 가장 큰 함정: “정적 디스패치”

## 3-1. 확장 함수는 오버라이드되지 않는다 (중요)

```kotlin
open class Parent
class Child : Parent()

fun Parent.foo() = "parent"
fun Child.foo() = "child"

val p: Parent = Child()
println(p.foo()) // "parent"
```

- 실제 객체는 `Child`
- 하지만 변수 타입이 `Parent`이므로 `Parent.foo()`가 호출됨

### 실무 포인트

- 확장 함수는 다형성(override) 기대하면 안 됨
- “타입별로 다르게 동작하게 만들고 싶다”면
    - 멤버 함수(override)로 설계하거나
    - `when` + sealed 같은 구조로 분기하는 게 더 안전

---

## 3-2. 멤버 함수 vs 확장 함수 충돌 시 우선순위

**멤버 함수가 항상 우선**입니다.

```kotlin
class A {
    fun hello() = "member"
}

fun A.hello() = "extension"

println(A().hello()) // "member"
```

### 실무 포인트

- 확장 함수 이름을 너무 일반적으로 만들면 충돌 위험
- 특히 Android/View/Context 확장 함수는 이름을 신중하게

---

# 4) 안드로이드 실무 예제 (필수)

## 4-1. View 가시성 유틸 (`visible`, `gone`)

```kotlin
fun View.visible() {
    visibility = View.VISIBLE
}

fun View.gone() {
    visibility = View.GONE
}
```

사용:

```kotlin
loadingView.visible()
contentView.gone()
```

### 실무 포인트

- UI 코드 가독성 확 올라감
- if/else 없이 표현이 명확해짐

---

## 4-2. Context Toast 유틸

```kotlin
fun Context.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}
```

사용:

```kotlin
requireContext().toast("저장 완료!")
```

---

## 4-3. EditText / TextView 안전 텍스트 추출

```kotlin
fun TextView.textString(): String = text?.toString().orEmpty().trim()
```

사용:

```kotlin
val email = emailEditText.textString()
```

### 실무 포인트

- `toString()` / `trim()` / null-safe 반복을 줄임

---

## 4-4. Fragment arguments 읽기 (간단 유틸)

```kotlin
fun Fragment.longArg(key: String): Long {
    return requireArguments().getLong(key)
}
```

사용:

```kotlin
val userId = longArg("arg_user_id")
```

> ⚠️ 실무에서는 key를 `const val`로 관리하는 패턴이 거의 필수
> 
> 
> (오타 방지)
> 

---

# 5) 확장 프로퍼티 (Extension Property)

확장 함수뿐 아니라 프로퍼티도 확장할 수 있습니다.

다만 **백킹 필드가 없어서 저장은 불가**이고, getter/setter로 계산만 가능합니다.

```kotlin
val String.lastIndex: Int
    get() = length - 1
```

사용:

```kotlin
println("Kotlin".lastIndex) // 5
```

### 실무 포인트

- 파생 값 계산용으로만 적합
- 상태 저장이 필요한 건 확장 프로퍼티로 해결 불가

---

# 6) 초기화 순서 & 상속에서의 “오버라이드 위험”과 연결

## 핵심 정리

- 확장 함수는 override가 안 되기 때문에 “상속 구조의 override 위험” 자체는 확장 함수에서 직접 발생하지 않음
- 하지만 실무에서 헷갈리는 포인트가 있습니다:

### 1) “확장 함수도 오버라이드될 거라고 착각”

- 실제론 **정적 디스패치**라서 변수 타입 기준으로 호출됨 (3장 함정)

### 2) “부모 init에서 open 멤버 호출” 위험은 여전히 존재

- 이건 확장 함수 문제가 아니라 “open 멤버” 문제
- 확장 함수로 우회하려고 하면 설계 의도가 흐려질 수 있음

> ✅ 결론
> 
> 
> 확장 함수는 “편의 API”로 쓰고, 다형성/초기화 위험 문제 해결용으로 쓰면 오히려 더 헷갈릴 수 있음
> 

---

# 7) 실무 설계 팁: 확장 함수 남용 방지

## 7-1. 확장 함수는 어디에 두는 게 좋은가?

- `core-ui` / `ui-common` 같은 모듈에 UI 확장 함수 모으기
- `core` 모듈에 공통 타입 확장 함수 모으기
- 파일명도 의도를 드러내기
    - `ViewExt.kt`, `ContextExt.kt`, `StringExt.kt`

## 7-2. 이름 충돌 방지 팁

- 너무 일반적인 이름(`show`, `hide`, `format`)은 충돌 가능성 높음
- 범위를 줄이거나, 의도를 드러내는 이름 사용
    - `visible()` / `gone()`처럼 명확한 동작명은 OK
    - `format()` 같은 이름은 도메인 구체화 필요

## 7-3. 접근 제한자 활용 (모듈/파일 단위)

- 외부 공개 필요 없으면 `internal`/`private`로 숨기기
- 모듈 내부에서만 쓰는 확장 함수는 `internal`이 깔끔함

---

# 8) 자주 나오는 질문

## Q1. 확장 함수는 기존 클래스를 진짜로 수정하나요?

- 아니요. 정적 함수로 컴파일되고, 호출 문법만 멤버처럼 보이게 합니다.

## Q2. 확장 함수는 override 되나요?

- 아니요. 정적 디스패치라서 다형성 오버라이드 대상이 아닙니다.

## Q3. 멤버 함수랑 이름이 겹치면?

- 멤버 함수가 항상 우선입니다.

## Q4. 확장 프로퍼티로 값을 저장할 수 있나요?

- 불가합니다. 백킹 필드가 없어서 계산(getter) 형태만 가능합니다.

---

# 최종 정리

- 확장 함수는 **“깔끔한 API”를 만드는 도구**이며 Android에서 특히 많이 쓰임
- 내부적으로는 정적 함수로 변환되어 **오버라이드/다형성은 기대하면 안 됨**
- 멤버 함수가 우선이므로 이름 충돌 주의
- 확장 프로퍼티는 저장 불가(계산만 가능)
- 모듈/패키지 구조를 정해두고 확장 함수를 관리하면 유지보수가 훨씬 쉬워짐

---
