# 함수형 프로그래밍과 확장 함수 (Kotlin)

---

## 1. 고차 함수(Higher-Order Functions)와 람다(Lambda)

### 1급 객체(First-Class Citizen)란?

코틀린에서 함수는 **1급 객체**이고, 1급 객체란 다음과 같다:

- 변수에 함수를 **저장**할 수 있다.
- 함수를 다른 함수의 **인자로 전달**할 수 있다.
- 함수를 다른 함수의 **반환값**으로 사용할 수 있다.

### 람다(Lambda)

람다는 **이름 없는 함수**이며 `{ 파라미터 -> 본문 }` 형태로 표현함

```kotlin
// 변수에 람다 저장
val greet: (String) -> String = { name -> "Hello, $name!" }
println(greet("Kotlin")) // Hello, Kotlin!

// 파라미터가 하나면 it으로 생략 가능
val double: (Int) -> Int = { it * 2 }
```

### 고차 함수(Higher-Order Function)

**다른 함수를 파라미터로 받거나 반환하는 함수**

```kotlin
fun operate(a: Int, b: Int, operation: (Int, Int) -> Int): Int {
    return operation(a, b)
}

val result = operate(3, 4) { x, y -> x + y } // 7
```

> **람다가 마지막 파라미터면 괄호 밖으로 꺼낼 수 있다 (trailing lambda).**

---

## 2. 인라인 함수 (Inline Functions)

### 고차 함수의 성능 문제

고차 함수를 사용하면 람다가 **익명 클래스 객체로 변환**되어 힙(heap)에 할당됩니다. 호출이 잦으면 객체 생성 비용이 쌓입니다.

### `inline` 키워드

`inline`을 붙이면 컴파일러가 **함수 본문 코드를 호출 지점에 직접 복사**합니다. 객체 생성 없이 성능을 유지할 수 있습니다.

```kotlin
inline fun repeat(times: Int, action: () -> Unit) {
    for (i in 0 until times) action()
}

// 컴파일 후: action() 호출 대신 action의 코드가 그 자리에 삽입됨
```

### `noinline`

인라인 함수 안에서 **특정 람다만 인라인 처리를 제외**하고 싶을 때 사용합니다. (해당 람다를 변수에 저장하거나 다른 함수에 전달해야 할 때 필요)

```kotlin
inline fun example(inlined: () -> Unit, noinline notInlined: () -> Unit) {
    inlined()                    // 인라인 처리됨
    someFunction(notInlined)     // 객체로 전달 가능
}
```

### `crossinline`

인라인 함수에서 람다를 **다른 실행 컨텍스트(예: 다른 람다나 객체 내부)** 에서 호출해야 할 때 사용합니다.
`return`을 통한 **비지역 반환(non-local return)을 금지**합니다.

```kotlin
inline fun runInBackground(crossinline task: () -> Unit) {
    Thread { task() }.start() // crossinline이 없으면 컴파일 에러
}
```

| 키워드 | 역할 |
|--------|------|
| `inline` | 함수 본문을 호출 지점에 복사 (성능 최적화) |
| `noinline` | 인라인 함수 내 특정 람다는 인라인 제외 |
| `crossinline` | 다른 컨텍스트에서 호출 허용, 단 비지역 return 금지 |

---

## 3. 스코프 함수 (Scope Functions)

스코프 함수는 **객체의 컨텍스트 내에서 코드 블록을 실행**하는 함수입니다.  
핵심 구분 기준은 두 가지입니다.

- **수신 객체를 어떻게 참조하는가**: `this` vs `it`
- **무엇을 반환하는가**: 블록의 결과 vs 수신 객체 자신

### 한눈에 비교

| 함수 | 수신 객체 참조 | 반환값 | 주요 사용 목적 |
|------|--------------|--------|--------------|
| `let` | `it` | 블록 결과 | null 체크, 결과 변환 |
| `run` | `this` | 블록 결과 | 초기화 + 결과 반환 |
| `with` | `this` | 블록 결과 | 여러 메서드 호출 묶기 |
| `apply` | `this` | 수신 객체 | 객체 설정(빌더 패턴) |
| `also` | `it` | 수신 객체 | 부수 효과(로깅, 검증) |

### 각 함수 예시

```kotlin
// let: null-safe 처리, it으로 참조
val name: String? = "Kotlin"
name?.let { println(it.uppercase()) } // "KOTLIN"

// run: this로 참조, 초기화 결과 반환
val result = "hello".run {
    println(this) // this = "hello"
    this.length   // 반환값: 5
}

// with: 이미 존재하는 객체의 메서드를 여러 번 호출
with(StringBuilder()) {
    append("Hello")
    append(" World")
    toString()
}

// apply: 객체 설정 후 객체 자신을 반환 (빌더 패턴)
val person = Person().apply {
    name = "Alice"
    age = 30
} // person 자체를 반환

// also: 객체를 건드리지 않고 부수 효과만 수행
val list = mutableListOf(1, 2, 3)
    .also { println("초기 리스트: $it") }
    .also { it.add(4) }
```

### 선택 방법

```
null check가 필요하다 → let
객체를 설정/초기화한다 → apply
결과를 변환해서 반환한다 → run / let
로그·검증 등 부수 효과 → also
여러 메서드를 한 번에 호출 → with
```

---

## 4. 확장 함수 (Extension Functions)

### 개념

기존 클래스 **소스 코드를 수정하지 않고** 새로운 메서드를 추가하는 것처럼 사용할 수 있는 문법입니다.

```kotlin
// String 클래스에 새 함수 추가
fun String.addExclamation(): String = "$this!"
 
println("Hello".addExclamation()) // Hello!
```

### 내부 동작: 컴파일러가 처리하는 방식

확장 함수는 **실제로 클래스를 수정하지 않습니다.**  
Kotlin 컴파일러가 확장 함수를 **"특정 객체에 종속되지 않는 독립적인 함수"** 로 변환합니다.
쉽게 말하면, `"Hello".addExclamation()` 이라고 써도 실제로는 `addExclamation("Hello")` 처럼 **수신 객체를 인자로 넘기는 일반 함수 호출**로 처리됩니다.

> **"독립적인 함수"** 란? — 특정 클래스의 인스턴스(객체)에 속하지 않고, 파일/패키지 수준에 따로 존재하는 함수입니다.  
> Java를 잠깐 언급하자면, Java의 `static` 메서드가 바로 이런 개념입니다. 클래스 객체를 만들지 않아도 호출할 수 있는 "독립 함수"죠. Kotlin의 확장 함수도 내부적으로 이와 동일하게 처리됩니다.

이 동작 방식 때문에 중요한 제약이 생깁니다.

### 오버라이딩 불가 (Override 위험성)

확장 함수는 **컴파일 시점에 어떤 함수를 호출할지 이미 결정**됩니다.  
일반 멤버 함수는 런타임에 실제 객체 타입을 보고 결정되지만, 확장 함수는 **변수의 선언 타입**만 봅니다.

```kotlin
open class Animal
class Dog : Animal()
 
fun Animal.speak() = "Animal speaks"
fun Dog.speak() = "Dog barks"
 
val animal: Animal = Dog()
println(animal.speak()) // "Animal speaks" ← Dog의 확장 함수가 호출되지 않음!
```

- 변수의 **선언 타입**이 기준이 됩니다.
- 멤버 함수(클래스 내부에 정의)와 확장 함수가 이름이 같으면 **멤버 함수가 우선**합니다.

### 초기화 순서 주의

확장 함수는 **클래스 외부, 파일/패키지 레벨에 독립적으로 존재**합니다. 클래스 초기화와 완전히 별개이므로 클래스 생성자나 `init` 블록과는 관계없습니다.  
단, `companion object` 내부에 확장 함수를 정의하거나 멤버 함수와 같은 이름을 쓸 때 충돌에 유의해야 합니다.
---

## 5. 참조 연산자 `::`

`::` 연산자는 함수나 프로퍼티를 **객체처럼 참조**할 때 사용합니다.

### 함수 참조

```kotlin
fun isEven(n: Int) = n % 2 == 0

val numbers = listOf(1, 2, 3, 4, 5)
val evens = numbers.filter(::isEven) // [2, 4]
// { n -> isEven(n) }과 동일
```

### 멤버 함수 참조

```kotlin
val lengths = listOf("a", "bb", "ccc").map(String::length) // [1, 2, 3]
```

### 생성자 참조

```kotlin
data class Person(val name: String)
val names = listOf("Alice", "Bob")
val people = names.map(::Person) // [Person("Alice"), Person("Bob")]
```

### 프로퍼티 참조

```kotlin
val str = "Kotlin"
val lengthProp = String::length
println(lengthProp.get(str)) // 6
```

> `::` 연산자는 람다 대신 **기존 함수를 그대로 재사용**할 때 코드를 간결하게 만들어 줍니다.

---

## 정리 요약

| 개념 | 핵심 포인트 |
|------|-----------|
| 고차 함수 & 람다 | 함수가 1급 객체; 변수 저장, 전달, 반환 가능 |
| `inline` | 람다를 객체로 만들지 않고 코드를 복사 → 성능 개선 |
| `noinline` | 특정 람다만 인라인 제외 |
| `crossinline` | 다른 컨텍스트 호출 허용, 비지역 return 금지 |
| 스코프 함수 | `this`/`it`, 반환값 기준으로 5가지 구분 |
| 확장 함수 | 정적 메서드로 변환; override 불가, 멤버 함수 우선 |
| `::` 참조 연산자 | 함수/프로퍼티/생성자를 객체처럼 참조 |
