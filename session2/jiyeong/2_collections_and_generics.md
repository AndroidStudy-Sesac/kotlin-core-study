# 컬렉션과 제네릭 (Kotlin)

---

## 1. 가변 / 불변 컬렉션의 분리

### 설계 철학

코틀린은 컬렉션을 **읽기 전용(Read-Only)** 과 **가변(Mutable)** 으로 명확히 분리합니다.  
Java는 컬렉션 수정 가능 여부를 런타임에 확인하지만, 코틀린은 **타입 시스템 레벨**에서 강제합니다.

| 인터페이스 | 특징 | 예시 |
|-----------|------|------|
| `List<T>` | 읽기 전용 (add, remove 없음) | `listOf(1, 2, 3)` |
| `MutableList<T>` | 읽기 + 쓰기 가능 | `mutableListOf(1, 2, 3)` |
| `Set<T>` | 읽기 전용 집합 | `setOf("a", "b")` |
| `MutableSet<T>` | 가변 집합 | `mutableSetOf("a", "b")` |
| `Map<K,V>` | 읽기 전용 맵 | `mapOf("key" to 1)` |
| `MutableMap<K,V>` | 가변 맵 | `mutableMapOf("key" to 1)` |

### 읽기 전용 ≠ 불변(Immutable)

**주의:** `List`는 수정 인터페이스가 없을 뿐, 실제 객체가 `MutableList`일 수 있습니다.  
완전히 불변인 컬렉션이 필요하다면 `Collections.unmodifiableList()` 또는 외부 라이브러리를 사용해야 합니다.

```kotlin
val mutable = mutableListOf(1, 2, 3)
val readOnly: List<Int> = mutable // 읽기 전용 뷰
mutable.add(4) // 가능
// readOnly.add(4) // 컴파일 에러
println(readOnly) // [1, 2, 3, 4] ← 실제 데이터는 변했다!
```

### 설계적 이점

- **의도 명확화**: 함수 파라미터가 `List`이면 "수정 안 함"을 보장합니다.
- **스레드 안전성 힌트**: 읽기 전용으로 공유하면 동시성 문제를 줄일 수 있습니다.

---

## 2. 컬렉션 연산 체이닝

코틀린 컬렉션은 풍부한 **함수형 연산자**를 제공합니다. 이 연산들은 **체이닝(chaining)** 이 가능합니다.

### 주요 연산자

#### `map` — 변환

각 원소를 다른 값으로 변환해 새 컬렉션을 반환합니다.

```kotlin
val numbers = listOf(1, 2, 3, 4)
val doubled = numbers.map { it * 2 } // [2, 4, 6, 8]
```

#### `filter` — 필터링

조건에 맞는 원소만 골라 새 컬렉션을 반환합니다.

```kotlin
val evens = numbers.filter { it % 2 == 0 } // [2, 4]
```

#### `forEach` — 순회 (부수 효과용)

각 원소에 대해 동작을 수행합니다. **반환값이 없습니다.**

```kotlin
numbers.forEach { println(it) }
```

#### `fold` — 누적 연산

초기값과 함께 원소를 하나씩 누적하여 단일 값으로 만듭니다.

```kotlin
val sum = numbers.fold(0) { acc, n -> acc + n } // 10
// acc: 누적값, n: 현재 원소
```

> `reduce`는 `fold`와 비슷하지만 초기값 없이 첫 원소를 초기값으로 사용합니다.

#### `flatMap` — 변환 후 펼치기

각 원소를 컬렉션으로 변환한 뒤 **하나의 리스트로 합쳐** 반환합니다.

```kotlin
val words = listOf("Hello World", "Kotlin")
val letters = words.flatMap { it.split(" ") }
// ["Hello", "World", "Kotlin"]
```

#### 자주 쓰는 기타 연산자

| 연산자 | 설명 | 예시 |
|--------|------|------|
| `first` / `last` | 첫/마지막 원소 | `list.first { it > 2 }` |
| `any` / `all` / `none` | 조건 만족 여부 (Boolean) | `list.any { it > 3 }` |
| `count` | 조건 만족 원소 수 | `list.count { it % 2 == 0 }` |
| `groupBy` | 키 기준 그룹화 → `Map` | `list.groupBy { it % 2 }` |
| `sortedBy` | 기준 정렬 | `list.sortedBy { it.name }` |
| `distinct` | 중복 제거 | `list.distinct()` |
| `zip` | 두 컬렉션을 쌍으로 묶기 | `a.zip(b)` |

### 체이닝 예시

```kotlin
val result = listOf(1, 2, 3, 4, 5, 6)
    .filter { it % 2 == 0 }   // [2, 4, 6]
    .map { it * it }            // [4, 16, 36]
    .fold(0) { acc, n -> acc + n } // 56
```

### 성능: `Sequence` 사용

일반 컬렉션 연산은 **각 단계마다 중간 컬렉션을 생성**합니다.  
데이터가 많다면 `asSequence()`를 사용해 **지연 평가(lazy evaluation)** 를 적용하세요.

```kotlin
// 중간 리스트를 만들지 않고 필요한 원소만 처리
val result = (1..1_000_000)
    .asSequence()
    .filter { it % 2 == 0 }
    .map { it * 2 }
    .take(5)
    .toList() // 여기서 실제 연산 시작
```

---

## 3. 제네릭 (Generics)

### 타입 파라미터 기초

제네릭은 클래스나 함수가 **다양한 타입을 안전하게 처리**할 수 있도록 합니다.

```kotlin
// T: 타입 파라미터
class Box<T>(val value: T)

val intBox = Box(42)          // Box<Int>
val strBox = Box("Hello")     // Box<String>
```

함수에서도 사용 가능합니다.

```kotlin
fun <T> identity(value: T): T = value
```

### 타입 상한(Upper Bound)

`<T : SomeType>` 형태로 타입 파라미터에 제약을 걸 수 있습니다.

```kotlin
fun <T : Comparable<T>> max(a: T, b: T): T = if (a > b) a else b
```

---

## 4. 공변성과 반공변성 (`out` / `in`)

제네릭의 **타입 안전성과 유연성**을 동시에 얻기 위한 개념입니다.

### 기본 문제: 무공변(Invariant)

기본적으로 `List<Dog>`는 `List<Animal>`의 하위 타입이 **아닙니다.**

```kotlin
open class Animal
class Dog : Animal()

val dogs: List<Dog> = listOf(Dog())
// val animals: List<Animal> = dogs // 컴파일 에러 (기본은 무공변)
```

### `out` — 공변성 (Covariance)

`out T`는 타입 파라미터 `T`를 **"생산(produce)"만** 합니다 (반환값으로만 사용).  
이 경우 `Producer<Dog>`를 `Producer<Animal>`로 취급할 수 있습니다.

```kotlin
interface Producer<out T> {
    fun produce(): T
}

val dogProducer: Producer<Dog> = ...
val animalProducer: Producer<Animal> = dogProducer // 가능!
```

> **암기법**: `out` = 읽기 전용 = 외부로 **내보낸다(output)**.  
> `List<out T>`가 코틀린 기본 `List`의 형태입니다.

### `in` — 반공변성 (Contravariance)

`in T`는 타입 파라미터 `T`를 **"소비(consume)"만** 합니다 (파라미터로만 사용).  
이 경우 `Consumer<Animal>`을 `Consumer<Dog>`로 취급할 수 있습니다.

```kotlin
interface Consumer<in T> {
    fun consume(item: T)
}

val animalConsumer: Consumer<Animal> = ...
val dogConsumer: Consumer<Dog> = animalConsumer // 가능!
// Animal을 소비할 수 있다면, Dog도 당연히 소비할 수 있다
```

> **암기법**: `in` = 쓰기 전용 = 안으로 **넣는다(input)**.  
> `Comparable<in T>`가 대표적인 예입니다.

### 핵심 규칙 정리

| 키워드 | 방향 | 허용 위치 | 예시 |
|--------|------|----------|------|
| 없음 (무공변) | 양방향 | 읽기 + 쓰기 | `MutableList<T>` |
| `out` (공변) | 하위 → 상위 | 반환값 전용 | `List<out T>` |
| `in` (반공변) | 상위 → 하위 | 파라미터 전용 | `Comparable<in T>` |

```
공변(out):  List<Dog> → 대입 가능 → List<Animal>  (좁은 타입 → 넓은 타입)
반공변(in): Consumer<Animal> → 대입 가능 → Consumer<Dog>  (넓은 타입 → 좁은 타입)
```

---

## 정리 요약

| 개념 | 핵심 포인트 |
|------|-----------|
| 가변 / 불변 분리 | 타입으로 수정 가능 여부를 명시; 읽기 전용 ≠ 진짜 불변 |
| 컬렉션 연산 | `map`, `filter`, `fold`, `flatMap` 등 체이닝; 성능 필요 시 `asSequence()` |
| 제네릭 기초 | `<T>`로 타입을 파라미터화; `<T : UpperBound>`로 제약 가능 |
| `out` (공변) | T를 반환에만 사용; 하위 타입 → 상위 타입으로 대입 가능 |
| `in` (반공변) | T를 파라미터에만 사용; 상위 타입 → 하위 타입으로 대입 가능 |
