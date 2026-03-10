# 3장. 컬렉션과 제네릭 (Collections & Generics)

## 전체 핵심 요약

- `List` vs `MutableList`는 단순 문법이 아니라 **설계(불변성/캡슐화) 선택**
- 컬렉션 연산(`map`, `filter`, `forEach`, `fold`, `flatMap`)은 “반복문”을 더 선언적으로 바꾸는 도구
- 연산 체이닝은 가독성이 좋지만, **중간 컬렉션 생성 비용**이 생길 수 있음 → 키워드: `Sequence`
- 제네릭은 타입 안전한 재사용의 핵심이며, `in/out`은 컬렉션/콜백 설계에서 자주 마주침
- Android 실무 연결:
    - UI 리스트 변환: DTO → UiModel, 정렬/필터링, 섹션 구성
    - Adapter/Compose `LazyColumn`은 내부적으로 컬렉션을 반복 처리
    - Repository/UseCase/Mapper에서 제네릭과 variance가 등장

---

# 1) 가변/불변의 분리: `List` vs `MutableList`

## 핵심

- `List<T>`: 읽기 전용(read-only) 인터페이스
- `MutableList<T>`: 수정 가능한 인터페이스 (`add`, `remove`, `clear` 등)

```kotlin
val a: List<Int> = listOf(1, 2, 3)
// a.add(4) // 불가

val b: MutableList<Int> = mutableListOf(1, 2, 3)
b.add(4) // 가능
```

## 실무 포인트

- 외부에 노출할 때는 `List`로 **읽기 전용**을 기본으로
- 내부에서는 `MutableList`로 관리하고, 외부에는 `List`로 노출하는 패턴이 자주 사용됨 (캡슐화)

```kotlin
class UserViewModel {
    private val _items = mutableListOf<String>()
    val items: List<String> get() = _items

    fun addItem(item: String) { _items.add(item) }
}
```

### 안드로이드 연결

- ViewModel 상태/리스트 관리에서 표준처럼 등장
- “외부에서 리스트를 마음대로 변경하지 못하게” 막는 설계

---

# 2) 컬렉션 연산 체이닝: `map`, `filter`, `forEach`, `fold`, `flatMap`

## 핵심

- `map`: 요소를 다른 형태로 변환
- `filter`: 조건을 만족하는 요소만 남김
- `forEach`: 각 요소에 대해 작업 수행 (반환값 없음)
- `fold`: 누적(accumulate)하여 하나의 값으로 만들기
- `flatMap`: 변환 결과가 컬렉션일 때 한 단계 펼치기

---

## 2-1) `map`

```kotlin
val names = listOf("a", "bb", "ccc")
val lengths = names.map { it.length } // [1, 2, 3]
```

### Android 실무 예시: DTO → UiModel 변환(Mapper 느낌)

```kotlin
data class UserResponse(val id: Long, val nickname: String)
data class UserUiModel(val id: Long, val title: String)

val uiModels = responses.map { UserUiModel(it.id, it.nickname) }
```

---

## 2-2) `filter`

```kotlin
val nums = listOf(1, 2, 3, 4, 5)
val evens = nums.filter { it % 2 == 0 } // [2, 4]
```

### Android 실무 예시: 검색 결과 필터링

```kotlin
val filtered = users.filter { it.nickname.contains(query, ignoreCase = true) }
```

---

## 2-3) `forEach`

```kotlin
listOf("A", "B").forEach { println(it) }
```

## 실무 포인트

- 순회하면서 UI 갱신/로그 등 부수 효과 작업
- `break/continue`가 필요하면 `for`가 더 적합할 수 있음 (선언형/명령형 선택)

---

## 2-4) `fold`

```kotlin
val nums = listOf(1, 2, 3)
val sum = nums.fold(0) { acc, n -> acc + n } // 6
```

### Android 실무 예시: 장바구니 총액 계산

```kotlin
data class CartItem(val price: Int, val count: Int)

val total = items.fold(0) { acc, item ->
    acc + (item.price * item.count)
}
```

---

## 2-5) `flatMap`

```kotlin
val groups = listOf(listOf(1, 2), listOf(3), listOf(4, 5))
val flat = groups.flatMap { it } // [1,2,3,4,5]
```

### Android 실무 예시: 섹션 리스트 → 단일 리스트 펼치기

```kotlin
data class Section(val title: String, val items: List<String>)

val allItems = sections.flatMap { it.items }
```

---

## 2-6) 체이닝 주의점 (실무 포인트)

- `map/filter`를 여러 번 이어 붙이면 중간 컬렉션이 계속 생성될 수 있음
- 데이터가 매우 크거나 성능이 민감하면 키워드: **`Sequence`(지연 연산)**

> ✅ 키워드만 체크 (심화는 필요 시)
> 
- `asSequence()`
- `toList()`

---

# 3) 제네릭(Generics) 기초: 타입 파라미터 & `in` / `out`

## 핵심

제네릭은 “타입을 변수처럼 받아서” 재사용 가능한 코드를 만드는 도구입니다.

```kotlin
class Box<T>(val value: T)

val intBox = Box(1)
val strBox = Box("hi")
```

### 실무 포인트

- Repository/UseCase/Result 래퍼 타입에서 많이 사용
- `Result<T>`, `List<T>`, `Response<T>` 같은 형태는 제네릭의 대표 사용

---

## 3-1) `out` (공변성, Producer)

- `out T`: T를 **내보내기만** 하는 타입(Producer)
- 읽기는 가능하지만, 쓰기는 제한됨
- “상위 타입으로 대입 가능하게” 만들어줌

```kotlin
interface Producer<out T> {
    fun get(): T
}
```

### 직관

- `T를 생산(produce)만 한다 → out`

---

## 3-2) `in` (반공변성, Consumer)

- `in T`: T를 **받기만** 하는 타입(Consumer)
- 넣기는 가능하지만, 꺼내기는 제한됨
- 콜백/Comparator 같은 곳에서 자주 등장

```kotlin
interface Consumer<in T> {
    fun accept(value: T)
}
```

### 직관

- `T를 소비(consume)만 한다 → in`

---

## 3-3) `in/out`을 어디서 체감하나? (Android/실무)

### 1) 콜백(리스너) 형태

- 어떤 타입을 “받아서 처리”하는 콜백은 `in` 관점

### 2) 데이터 제공자(Provider) 형태

- 어떤 타입을 “반환해서 제공”하는 API는 `out` 관점

### 3) Kotlin 표준 라이브러리에서 많이 보임

- `List<out T>` 처럼 읽기 중심 컬렉션은 out 성격
- 함수 타입 파라미터도 입력/출력 방향성 개념이 있음

---

# 4) 안드로이드 실무 예제 모음 (컬렉션 중심)

## 예제 1) RecyclerView/Compose에 넣기 전에 UiModel 변환 + 필터

```kotlin
val uiModels = responses
    .filter { it.nickname.isNotBlank() }
    .map { UserUiModel(it.id, it.nickname) }
```

## 예제 2) 검색어 기반 필터 + 정렬

```kotlin
val result = users
    .filter { it.nickname.contains(query, ignoreCase = true) }
    .sortedBy { it.nickname }
```

> `sortedBy`는 보너스 키워드 (자주 쓰지만 필수는 아님)
> 

---

# 최종 정리

- 컬렉션은 **불변/가변을 분리**해서 설계하는 게 안전함 (`List` 기본)
- `map/filter/fold/flatMap`는 UI 변환/검색/집계에서 실무 사용 빈도 매우 높음
- 체이닝이 길어지면 중간 컬렉션 생성 비용을 의식 → 키워드 `Sequence`
- 제네릭은 Repository/Result/Wrapper 설계에서 필수
- `in/out`은 “받는 쪽(consumer)” vs “주는 쪽(producer)”로 감 잡으면 쉬움

---
