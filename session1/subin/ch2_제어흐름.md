# 2장. 제어 흐름 (Control Flow)

## 전체 핵심 요약

- `if`, `when`은 **문장(statement)** 이 아니라 **식(expression)** 으로도 사용 가능
- `when`은 값 비교 / 범위 검사 / 타입 검사까지 가능 (코틀린 제어 흐름 핵심)
- `for`, `while`, `do-while` + `break`/`continue`/라벨로 반복 제어 가능
- 실무에서는 `for`를 직접 쓰기도 하지만, Android에서는 `LazyColumn`, `forEach`, 상태 기반 렌더링으로 대체되는 경우도 많음
- 코틀린다운 반복의 핵심은 `withIndex()`, `repeat()`, `until`, `downTo`, `step`

---

## 1) 식(Expression)으로서의 제어문: `if` / `when`

## 핵심
코틀린에서 `if`, `when`은 단순 분기문이 아니라 **값을 반환하는 식(Expression)** 으로 사용할 수 있습니다.

### 왜 중요한가?

- 변수 초기화에 바로 사용 가능
- `return`에 바로 연결 가능
- 임시 변수/중복 분기 줄어듦
- 코드가 짧고 명확해짐

---

## 1-1. `if`를 식(Expression)으로 사용

```kotlin
val score = 85

val result = if (score >= 60) {
    "Pass"
} else {
    "Fail"
}
```

### 실무 포인트

- 간단한 검증 결과 메시지
- UI 텍스트/상태 결정
- 버튼 활성화 여부 계산

### Android 예시 (입력 검증 메시지)

```kotlin
fun getNicknameErrorMessage(nickname: String): String? {
    return if (nickname.isBlank()) {
        "닉네임을 입력해주세요."
    } else if (nickname.length < 2) {
        "닉네임은 2자 이상이어야 합니다."
    } else {
        null
    }
}
```

---

## 1-2. `when`을 식(Expression)으로 사용

```kotlin
val statusCode = 404

val message = when (statusCode) {
    200 -> "OK"
    404 -> "Not Found"
    500 -> "Server Error"
    else -> "Unknown"
}
```

### 실무 포인트

- 상태값/코드값 → UI 문구 매핑
- 입력값 검증 결과 분기
- 권한 상태/에러 상태 처리

---

## 1-3. `when`의 핵심 패턴 (꼭 알아야 함)

### ① 값 비교

```kotlin
val day = 1

val dayName = when (day) {
    1 -> "월"
    2 -> "화"
    3 -> "수"
    else -> "기타"
}
```

---

### ② 여러 값 한 번에 매칭

```kotlin
val code = 201

val category = when (code) {
    200, 201 -> "성공"
    400, 401, 403 -> "클라이언트 오류"
    500, 502 -> "서버 오류"
    else -> "기타"
}
```

---

### ③ 범위 검사 (`in`, `!in`)

```kotlin
val score = 87

val grade = when (score) {
    in 90..100 -> "A"
    in 80..89 -> "B"
    in 70..79 -> "C"
    in 0..69 -> "F"
    else -> "잘못된 점수"
}
```

### 실무 포인트

- 점수/레벨/가격 구간 분기
- 입력값 범위 검증

---

### ④ 타입 검사 (`is`) + 스마트 캐스트

```kotlin
fun describe(value: Any): String {
    return when (value) {
        is String -> "문자열 길이: ${value.length}"
        is Int -> "정수 값: $value"
        is Boolean -> "불리언: $value"
        else -> "알 수 없는 타입"
    }
}
```

> `is` 분기 안에서는 스마트 캐스트가 적용되어 별도 형변환 없이 멤버 접근 가능
> 

---

## 1-4. `when` without argument (조건식 기반 분기)

`if - else if - else` 체인을 더 깔끔하게 바꿀 때 유용합니다.

```kotlin
val score = 72

val grade = when {
    score >= 90 -> "A"
    score >= 80 -> "B"
    score >= 70 -> "C"
    else -> "F"
}
```

### 실무 포인트

- 복합 조건 분기
- UI 상태 표시 문구 결정
- 폼 검증 우선순위 처리

---

## 1-5. `when` 실무 예제 (Android)

### 권한 상태 문구/액션 분기 (예: 위치 권한)

```kotlin
enum class PermissionState {
    GRANTED, DENIED, PERMANENTLY_DENIED
}

fun getPermissionMessage(state: PermissionState): String {
    return when (state) {
        PermissionState.GRANTED -> "권한이 허용되었습니다."
        PermissionState.DENIED -> "권한이 필요합니다. 다시 요청해주세요."
        PermissionState.PERMANENTLY_DENIED -> "설정에서 권한을 직접 허용해주세요."
    }
}
```

### 실무 포인트

- 권한 상태 → 사용자 안내 문구 매핑
- 분기 로직이 한 곳에 모여서 유지보수 쉬움

---

## 1-6. 왜 안드로이드 실무에서는 `when`/반복문을 덜 직접 쓰는 느낌일까?

> ✅ 결론: 안 쓴 게 아니라, **프레임워크/컴포넌트가 반복을 대신 표현해주는 경우가 많음**
> 

### 자주 보이는 패턴들

- **Jetpack Compose**
    - `Column { ... }`
    - `LazyColumn { items(list) { ... } }`
- **RecyclerView**
    - Adapter가 내부적으로 반복 바인딩 수행
- **컬렉션 함수**
    - `forEach`, `map`, `filter`
- **상태 기반 UI**
    - `when (uiState)`로 큰 분기만 하고, 내부 렌더링은 컴포저블/어댑터가 처리

### 예시

```kotlin
@Composable
fun UserList(users: List<User>) {
    LazyColumn {
        items(users) { user ->
            UserItem(user)
        }
    }
}
```

여기서는 내가 `for (user in users)`를 직접 안 써도 실제로는 **리스트를 순회하며 UI를 반복 생성**하는 개념입니다.

---

# 2) 반복문 기본: `for`, `while`, `do-while`

## 2-1. `for`

코틀린에서 가장 많이 쓰는 반복문입니다.

컬렉션, 범위(range), 배열 등을 순회할 때 사용합니다.

```kotlin
for (i in 1..3) {
    println(i)
}
```

---

## 2-2. `while`

조건을 먼저 검사하고 반복합니다.

```kotlin
var count = 0

while (count < 3) {
    println(count)
    count++
}
```

### 실무 포인트

- 재시도 로직
- 특정 조건 만족까지 반복
- 상태 변화 감시 루프 (비동기/코루틴은 다음 회차에서 정리할 예정)

---

## 2-3. `do-while`

최소 1번은 실행한 뒤 조건을 검사합니다.

```kotlin
var count = 0

do {
    println(count)
    count++
} while (count < 3)
```

### 실무 포인트

- “최소 1회 실행 보장”이 필요한 로직
- 사용자 입력 재시도/확인 루프 등

---

# 3) 반복 제어 키워드: `break`, `continue`, 라벨(label)

## 3-1. `break`

가장 가까운 반복문을 즉시 종료

```kotlin
for (i in 1..10) {
    if (i == 5) break
    println(i)
}
```

---

## 3-2. `continue`

현재 반복만 건너뛰고 다음 반복으로 진행

```kotlin
for (i in 1..5) {
    if (i == 3) continue
    println(i)
}
```

---

## 3-3. 라벨(Label)과 함께 사용하는 `break` / `continue`

중첩 반복문에서 바깥 루프를 제어할 때 사용

```kotlin
outer@ for (i in 1..3) {
    for (j in 1..3) {
        if (j == 2) continue@outer
        println("i=$i, j=$j")
    }
}
```

### 실무 포인트

- 중첩 탐색 로직
- 특정 조건에서 바깥 반복으로 즉시 이동
- 파싱/검사 로직에서 가끔 유용

> 너무 남용하면 가독성이 떨어질 수 있으므로 필요할 때만 사용
> 

---

# 4) 코틀린다운 반복 (실무 관용구)

## 4-1. 단순 아이템 순회

```kotlin
val users = listOf("A", "B", "C")

for (user in users) {
    println(user)
}
```

### 실무 포인트

- 단순 렌더링/출력/검증
- 인덱스가 필요 없을 때 가장 깔끔함

---

## 4-2. 인덱스와 함께 순회 (`withIndex()`)

```kotlin
val users = listOf("A", "B", "C")

for ((index, user) in users.withIndex()) {
    println("$index: $user")
}
```

### 실무 포인트 (Android)

- RecyclerView 항목 번호 표시
- 구분선/마진 처리 (첫 번째/마지막 아이템 판별)
- 순서 기반 스타일 적용

### 예시: 첫 번째 아이템 강조 처리

```kotlin
for ((index, item) in items.withIndex()) {
    if (index == 0) {
        showHighlightedItem(item)
    } else {
        showNormalItem(item)
    }
}
```

---

## 4-3. 단순 반복 작업 (`repeat(n)`)

```kotlin
repeat(3) {
    println("Hello")
}
```

`it`를 사용하면 반복 인덱스를 받을 수 있음

```kotlin
repeat(3) { index ->
    println("index = $index")
}
```

### 실무 포인트 (Android)

- 스켈레톤 UI placeholder N개 생성
- 테스트 데이터 반복 생성
- 제한 횟수 재시도 로직

### 예시: placeholder 3개 생성

```kotlin
repeat(3) {
    addLoadingPlaceholder()
}
```

---

# 5) 범위(Range)와 진행(Progression): `..`, `until`, `downTo`, `step`

## 5-1. `..` (끝 포함)

```kotlin
for (i in 1..5) {
    println(i) // 1,2,3,4,5
}
```

---

## 5-2. `until` (끝 미포함)

```kotlin
for (i in 0 until 5) {
    println(i) // 0,1,2,3,4
}
```

### 실무 포인트

- 인덱스 반복에서 특히 자주 사용 (`0 until list.size`)

---

## 5-3. `downTo` (감소 반복)

```kotlin
for (i in 5 downTo 1) {
    println(i) // 5,4,3,2,1
}
```

---

## 5-4. `step` (증감 간격 지정)

```kotlin
for (i in 1..10 step 2) {
    println(i) // 1,3,5,7,9
}
```

### 실무 포인트

- 페이지 계산
- 특정 간격 처리
- 짝수/홀수 인덱스 분리 처리

---

# 6) `in` / `!in` 연산자 (조건문 + 반복문에서 중요)

## 핵심

`in`, `!in`은 `if`, `when`, `for`에서 모두 자주 등장합니다.

```kotlin
val score = 85

if (score in 0..100) {
    println("유효한 점수")
}
```

```kotlin
val blockedNames = setOf("admin", "root")

if ("guest" !in blockedNames) {
    println("사용 가능")
}
```

## 실무 포인트

- 입력값 범위 검증
- 허용/금지 목록 체크
- 분기 조건 간결화

---

# 7) 실무에서 반복문 대신 자주 쓰는 방식

> ✅ 중요
> 
> 
> 안드로이드 실무에서는 “반복문을 안 쓰는 것처럼 보이는” 경우가 많음
> 
> 실제로는 **반복의 표현 방식이 바뀐 것**
> 

## 자주 쓰는 방식

- `forEach`
- `map`, `filter` (컬렉션 처리)
- RecyclerView Adapter 바인딩
- Compose `Column`, `LazyColumn`

### 예시 1) `forEach`

```kotlin
users.forEach { user ->
    println(user.name)
}
```

### 예시 2) Compose `LazyColumn` (반복 표현의 대체)

```kotlin
@Composable
fun UserList(users: List<User>) {
    LazyColumn {
        items(users) { user ->
            UserItem(user)
        }
    }
}
```

## 언제 `for`가 더 좋을까? (실무 감각)

- `break`, `continue`가 필요할 때
- 인덱스를 명확하게 써야 할 때 (`withIndex()`)
- 반복 제어가 중요한 로직일 때
- 팀 코드 스타일상 명확한 반복문이 더 읽기 좋을 때

---

# 8) 코루틴/비동기와 연결되는 키워드

> 이번 챕터에서는 깊게 다루지 않고 session2회차에서 다룰 예정입니다! 
> 
- `suspend` 함수 안 제어 흐름
- `return@label` (람다 + 라벨 리턴)
- `repeatOnLifecycle`
- `Flow` + `when(uiState)`
- cancellation-safe loop (`isActive`)

---

# 최종 정리 (실무 포인트 중심)

- `if`, `when`은 **식(Expression)** 으로 쓸 수 있어서 변수 초기화/return에 강력함
- `when`은 값 비교 + 범위(`in`) + 타입 검사(`is`)까지 커버하는 코틀린 핵심 분기문
- `for`, `while`, `do-while` + `break`/`continue`/라벨로 반복 제어 가능
- 코틀린다운 반복 핵심은 `withIndex()`, `repeat()`, `until`, `downTo`, `step`
- 안드로이드에서는 반복을 직접 쓰기보다 `RecyclerView`, `LazyColumn`, 컬렉션 함수로 표현하는 경우가 많음
- `for`와 컬렉션 함수는 대체 관계가 아니라 **상황에 맞는 선택지**
