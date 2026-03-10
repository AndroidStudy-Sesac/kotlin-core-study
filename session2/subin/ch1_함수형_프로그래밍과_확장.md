# 1장. 함수형 프로그래밍과 확장

## 전체 핵심 요약

- 고차 함수/람다 → 함수를 값처럼 다루는 Kotlin의 핵심 문법
- 인라인 함수 → 고차 함수 성능/메모리 최적화 도구 (`inline`, `noinline`, `crossinline`)
- 스코프 함수 → `this` vs `it`, 반환값 차이를 이해하면 실무 가독성이 크게 좋아짐
- 확장 함수 → 기존 클래스에 “내가 원하는 API” 추가 (실제로는 정적 함수로 컴파일됨)
- 참조 연산자 `::` → 함수/프로퍼티 참조, 콜백/바인딩/리플렉션 입문 포인트
- Android 실무 연결:
    - View/Compose/UI 구성에서 람다/스코프 함수가 폭발적으로 등장
    - 확장 함수로 View/Context/Intent/Fragment 유틸을 많이 만든다
    - 인라인은 `crossinline` 포함해서 “고차함수 + 리턴” 이슈에서 자주 마주친다

---

# 1) 고차 함수와 람다

## 핵심

- **고차 함수(Higher-order function)**: 함수를 **파라미터로 받거나**, **함수를 반환하는 함수**
- **람다(Lambda)**: 이름 없는 함수 표현식 (함수 값)

```kotlin
val add: (Int, Int) -> Int = { a, b -> a + b }

fun operate(a: Int, b: Int, op: (Int, Int) -> Int): Int {
    return op(a, b)
}

val result = operate(3, 4, add) // 7
```

## 실무 포인트

- 콜백/이벤트 처리의 기본 형태
- “동작을 인자로 받는 API” 설계의 핵심
- Android에서 클릭/리스너/비동기 콜백이 대부분 람다 기반

---

## 안드로이드 예제 1: 클릭 리스너는 결국 람다

```kotlin
button.setOnClickListener {
    // 클릭 시 실행할 동작
    logEvent("clicked")
}
```

---

## 안드로이드 예제 2: 유효성 검사 함수를 주입하는 패턴

```kotlin
fun validate(input: String, rule: (String) -> Boolean): Boolean {
    return rule(input)
}

val isEmail = validate("test@example.com") { text ->
    text.contains("@") && text.contains(".")
}
```

- 화면/폼마다 검증 규칙이 달라지는 경우, 규칙을 함수로 분리하기 좋음

---

## 람다에서 자주 보는 문법 포인트

### 1) 마지막 인자가 함수면 바깥으로 뺄 수 있음 (Trailing Lambda)

```kotlin
fun doSomething(action: () -> Unit) {
    action()
}

doSomething {
    println("run")
}
```

### 2) 파라미터 1개면 `it` 사용 가능

```kotlin
val length = listOf("a", "ab", "abc").map { it.length }
```

---

# 2) 인라인 함수: `inline`, `noinline`, `crossinline`

## 핵심

고차 함수를 많이 쓰면 객체(람다)가 생성되고 호출 오버헤드가 생길 수 있어요.

그래서 Kotlin은 **inline**으로 고차 함수 성능을 최적화할 수 있게 해줍니다.

- `inline` : 함수 본문을 호출 지점에 “복사”해서 오버헤드 줄임
- `noinline` : 인라인 금지 (람다를 변수에 저장/전달해야 할 때 필요)
- `crossinline` : 람다 안에서 **non-local return**(바깥 함수 return) 금지

---

## 2-1. `inline` 기본 예시

```kotlin
inline fun measure(action: () -> Unit) {
    val start = System.currentTimeMillis()
    action()
    val end = System.currentTimeMillis()
    println("time = ${end - start}ms")
}

measure {
    // 측정할 코드
}
```

## 실무 포인트

- 고차 함수 기반 유틸(로깅/측정/트레이싱)에서 유용
- Kotlin 표준 라이브러리도 inline을 적극 활용 (`let`, `run`, `apply`, `also` 등)

---

## 2-2. `noinline`이 필요한 경우

인라인 람다는 “복사”되기 때문에, 람다를 값으로 저장/전달하려면 제한이 생길 수 있어요.

그럴 때 `noinline`으로 인라인을 막습니다.

```kotlin
inline fun runTwo(
    first: () -> Unit,
    noinline second: () -> Unit
) {
    first()
    listOf(second).forEach { it() }
}
```

## 실무 포인트

- 콜백을 컬렉션에 저장하거나 다른 API로 넘겨야 할 때 등장

---

## 2-3. `crossinline`이 필요한 경우 (안드로이드에서 체감 큼)

인라인 람다는 바깥 함수로 `return`(non-local return)이 가능해요.

그런데 람다를 **다른 곳에서 실행**(예: Runnable/Thread/Listener)하려고 하면 non-local return이 위험해져서 막아야 합니다.

```kotlin
inline fun post(crossinline action: () -> Unit) {
    val r = Runnable { action() }
    r.run()
}
```

- `crossinline`은 “람다 안에서 바깥으로 탈출(return) 못 하게” 막는 키워드

## 실무 포인트

- 고차 함수 + “나중에 실행되는 구조(Runnable/Callback)” 조합에서 자주 등장
- 코루틴/비동기 쪽에서도 이 개념이 연결됨 (키워드만 체크)

---

# 3) 스코프 함수 (Scope Functions)

## 핵심

스코프 함수는 “객체 컨텍스트 안에서 코드 블록을 실행”하게 해주고,

**this/it**와 **반환값**이 각각 다릅니다.

- `let` / `run` / `with` / `apply` / `also`

---

## 3-1. 먼저 이 2가지만 기억하면 됨

### ✅ 컨텍스트: `this` vs `it`

- `this`: `run`, `with`, `apply`
- `it`: `let`, `also`

### ✅ 반환값: 객체 반환 vs 마지막 줄 반환

- 객체 반환: `apply`, `also`
- 마지막 줄 반환: `let`, `run`, `with`

---

## 3-2. 각 스코프 함수 한 줄 정의 + 예시

### `let` (it / 마지막 줄 반환)

- **null-safe 처리** + 변환(map) 느낌에 강함

```kotlin
val length = nickname?.let { it.length }
```

---

### `run` (this / 마지막 줄 반환)

- 객체 컨텍스트에서 **계산 결과**를 만들고 반환할 때 유용

```kotlin
val result = user.run {
    name + age
}
```

---

### `with` (this / 마지막 줄 반환) *확장함수 아님*

- “대상 객체에 대해 여러 줄 작업” 후 결과 반환

```kotlin
val result = with(user) {
    name + age
}
```

---

### `apply` (this / 객체 반환)

- 객체 설정/초기화(빌더 스타일)에 최강

```kotlin
val intent = Intent().apply {
    putExtra("id", 1L)
}
```

---

### `also` (it / 객체 반환)

- 원본 객체를 그대로 유지하면서 **중간 로깅/부수효과** 넣을 때 유용

```kotlin
val list = mutableListOf(1, 2, 3)
    .also { println("before = $it") }
    .also { it.add(4) }
```

---

## 안드로이드 실무 예제

### 예제 1) Fragment `newInstance()` / Bundle 설정은 `apply`

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

- `apply`는 “객체 설정 후 객체 반환”이라 이 패턴이 깔끔해짐

---

### 예제 2) null-safe + 변환/반환은 `let`

```kotlin
val title = intent.getStringExtra("title")
    ?.let { it.trim() }
    ?: "default"
```

---

## 실무 포인트 (선택 기준)

- null-safe 처리/변환 → `let`
- 객체 설정 후 반환 → `apply`
- 중간 로깅/부수 효과 → `also`
- 객체 컨텍스트에서 결과 계산 → `run` / `with`

> ✅ 스코프 함수는 “취향”이 아니라 “의도 표현” 도구
> 
> 
> (남용하면 오히려 가독성 떨어지니 목적이 명확할 때만)
> 

---

# 4) 확장 함수 (Extension Functions)

## 핵심

확장 함수는 기존 클래스에 새 메서드를 “추가하는 것처럼” 보이지만,

실제로는 **정적(static) 함수로 컴파일**됩니다.

```kotlin
fun String.lastChar(): Char = this[this.length - 1]

val c = "Kotlin".lastChar()
```

## 실무 포인트

- Android에서 `Context`, `View`, `Fragment`, `Intent` 등에 확장함수 많이 만든다
- 유틸 함수를 클래스 밖으로 빼서 API처럼 쓰게 해줌
- “진짜 멤버 추가”가 아니라서 오버라이드/다형성에서 함정이 있음 (아래 참고)

---

## 4-1. Android 실무 예제 1: `View.visible()/gone()`

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

---

## 4-2. Android 실무 예제 2: `Context.toast()`

```kotlin
fun Context.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}
```

사용:

```kotlin
requireContext().toast("Hello")
```

---

## 4-3. 확장 함수 동작 원리 (중요)

- 확장 함수는 **정적 디스패치(static dispatch)** 입니다.
- 즉, 호출 시점의 “변수 타입”을 기준으로 어떤 함수가 호출될지 결정됩니다.

### 오버라이드가 “되는 것처럼 보이지만” 아니다 (주의)

```kotlin
open class Parent
class Child : Parent()

fun Parent.foo() = "parent"
fun Child.foo() = "child"

val p: Parent = Child()
println(p.foo()) // "parent"
```

- 런타임 객체는 `Child`지만, 변수 타입이 `Parent`라서 `Parent.foo()`가 호출됨

> ✅ 결론
> 
> 
> 확장 함수는 다형성 오버라이드 대상이 아니다.
> 
> “멤버처럼 보이게 해주는 문법 설탕”에 가깝다.
> 

---

## 4-4. 초기화 순서 / 오버라이드 위험성 연결

- 확장 함수 자체는 상속 오버라이드가 안 되지만,
- 클래스의 `open` 멤버 호출/초기화 순서 문제와 결합되면 오해가 생길 수 있음

---

# 5) 참조 연산자 `::`

## 핵심

`::`는 함수/프로퍼티를 **참조(Reference)** 로 가져오는 문법입니다.

- 함수 참조: `::functionName`
- 프로퍼티 참조: `Class::property`

---

## 5-1. 함수 참조 예시

```kotlin
fun isEven(n: Int): Boolean = n % 2 == 0

val result = listOf(1, 2, 3, 4).filter(::isEven)
```

- `{ n -> isEven(n) }` 대신 `::isEven`으로 더 깔끔하게 표현

---

## 5-2. 프로퍼티 참조 예시

```kotlin
data class User(val name: String)

val names = listOf(User("A"), User("B")).map(User::name)
```

- `{ user -> user.name }` 대신 `User::name`

---

## 실무 포인트 (Android)

- 콜백/리스너에서 “함수 자체를 전달”할 때
- RecyclerView/Compose에서 변환 로직을 깔끔하게 만들 때
- 테스트에서 함수 참조로 가독성 올릴 때

---

# 스코프 함수 5개 완전 정리

| 함수 | 컨텍스트 객체 | 람다 파라미터 이름 | 반환값 | 대표 용도(실무) | null-safe | 한 줄 기억법 | 예시 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `let` | `it` | `it` (또는 이름 지정) | **마지막 줄 결과** | **null 처리**, 값 변환(map), 짧은 스코프 | ✅ ( `?.let` ) | “null이면 안 돌고, 돌면 결과 뽑음” | `val len = s?.let { it.length }` |
| `run` | `this` | `this` | **마지막 줄 결과** | 객체 컨텍스트에서 **계산해서 결과 리턴**, 임시 스코프 | ⭕ ( `?.run` 가능) | “this로 작업하고 결과 리턴” | `val url = config.run { "$host:$port" }` |
| `with` | `this` | `this` | **마지막 줄 결과** | 객체 대상으로 여러 줄 작업 후 **결과 리턴** (non-extension) | ❌ (대상 null이면 먼저 처리 필요) | “대상을 넣고 this로 작업” | `val r = with(user) { name + age }` |
| `apply` | `this` | `this` | **객체 자기 자신** | **초기화/설정(Builder 스타일)**, `Bundle/Intent` 세팅 | ⭕ ( `?.apply` 가능하지만 주의) | “설정하고 나 자신 반환” | `val i = Intent().apply { putExtra("id", 1) }` |
| `also` | `it` | `it` | **객체 자기 자신** | **부수효과(로그/디버그/추가 작업)** 끼워넣기 | ⭕ ( `?.also` 가능) | “중간에 끼워넣고 나 자신 반환” | `val x = list.also { println(it) }` |

---

## 실무에서 가장 흔한 조합 3개

### 1) null-safe + 변환: `?.let { } ?: default`

```kotlin
val title = intent.getStringExtra("title")
    ?.let { it.trim() }
    ?: "default"
```

### 2) 객체 설정/초기화: `apply`

```kotlin
val bundle = Bundle().apply {
    putLong("userId", 1L)
}
```

### 3) 중간 로그/디버깅: `also`

```kotlin
val result = users
    .also { println("before size=${it.size}") }
    .filter { it.isActive }
    .also { println("after size=${it.size}") }
```

---

# 최종 정리

- Android 코드는 사실상 “람다 + 스코프 함수 + 확장 함수”로 이루어져 있음
- 스코프 함수는 “의도 표현” 도구 (null 처리/설정/부수효과/결과 계산)
- 확장 함수는 편하지만 정적 디스패치라 다형성 착각 주의
- `inline/noinline/crossinline`은 고차함수 성능/제어 흐름(리턴) 관점에서 중요
- `::`는 함수/프로퍼티 참조로 컬렉션 연산/콜백 코드 가독성 올려줌

---
