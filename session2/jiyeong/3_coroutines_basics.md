# 코루틴 기초 (Kotlin Coroutines)

---

## 1. 코루틴이란? (스레드와의 차이)

### 전통적인 스레드(Thread)의 문제

안드로이드에서 네트워크 요청이나 DB 작업은 **Main Thread(UI Thread)에서 실행하면 안 됩니다.**  
전통적으로 별도 스레드를 만들어 처리했지만, 스레드는 비용이 큽니다.

- 스레드는 생성 비용이 크고 OS 자원을 사용합니다.
- 스레드 간 **Context Switching** 비용이 있습니다. (커널이 레지스터, 스택 등 상태를 저장/복원)
- 수천 개의 스레드를 동시에 유지하기 어렵습니다.

### 코루틴: 경량 비동기 작업 단위

코루틴은 **스레드 위에서 동작하지만 스레드보다 훨씬 가볍습니다.**

- 코루틴은 스레드를 차단(block)하지 않고 **일시 중단(suspend)** 합니다.
- 하나의 스레드에서 수천 개의 코루틴이 동시에 실행될 수 있습니다.
- Context Switching이 **JVM 레벨(코루틴 스케줄러)** 에서 이루어지므로 OS 커널 전환보다 훨씬 저렴합니다.

```
Thread A: [코루틴1 실행] → (suspend) → [코루틴2 실행] → (resume 코루틴1) → ...
```

> 코루틴1이 네트워크 응답을 기다리는 동안 Thread A는 다른 코루틴을 실행합니다.  
> Thread는 블록되지 않습니다.

### 비교 요약

| | 스레드(Thread) | 코루틴(Coroutine) |
|--|--------------|-----------------|
| 생성 비용 | 크다 (OS 자원) | 매우 작다 (객체 수준) |
| 동시 실행 수 | 수십 ~ 수백 | 수만 개 가능 |
| 블로킹 | 대기 중 스레드 점유 | 대기 중 스레드 반환 |
| Context Switch | OS 커널 수준 | JVM/코루틴 스케줄러 |

---

## 2. `suspend` 키워드: 일시 중단과 재개

### `suspend` 함수란?

`suspend` 키워드가 붙은 함수는 **코루틴 안에서만 호출 가능**하며, 실행 도중 **일시 중단(suspend)** 되었다가 나중에 **재개(resume)** 될 수 있습니다.

```kotlin
suspend fun fetchData(): String {
    delay(1000) // 1초 일시 중단 (스레드는 블록되지 않음)
    return "Data"
}
```

> `delay()`는 `Thread.sleep()`과 달리 스레드를 블록하지 않습니다.

### 내부 동작: CPS 변환 (Continuation Passing Style)

`suspend` 함수는 컴파일 시 **Continuation**을 파라미터로 추가하는 방식으로 변환됩니다.

```kotlin
// 코틀린 코드
suspend fun fetchData(): String { ... }

// 컴파일 후 (의사 코드)
fun fetchData(continuation: Continuation<String>): Any {
    // 일시 중단 시 COROUTINE_SUSPENDED를 반환
    // 재개 시 continuation.resume(result)를 호출
}
```

- **일시 중단 시**: 현재 상태(지역 변수, 실행 위치)를 `Continuation` 객체에 저장하고 스레드를 반환합니다.
- **재개 시**: 저장된 `Continuation`을 통해 중단된 지점부터 다시 실행합니다.

덕분에 코루틴은 비동기 코드를 **동기 코드처럼** 읽기 쉽게 작성할 수 있게 합니다.

```kotlin
// 콜백 지옥 없이, 순차적으로 읽힘
val user = fetchUser()        // 네트워크 대기 중 suspend
val posts = fetchPosts(user)  // 위 결과를 받아 또 suspend
display(user, posts)
```

---

## 3. Dispatchers: 코루틴이 실행될 스레드 풀

코루틴은 **어떤 스레드(풀)에서 실행할지** `CoroutineDispatcher`로 지정할 수 있다.

### 종류

#### `Dispatchers.Main`

- **메인 스레드(UI 스레드)** 에서 실행합니다.
- UI 업데이트, `LiveData` 관찰, `RecyclerView` 갱신 등에 사용합니다.
- **블로킹 작업을 여기서 하면 ANR 발생합니다.**

```kotlin
withContext(Dispatchers.Main) {
    textView.text = "완료" // UI 업데이트
}
```

#### `Dispatchers.IO`

- **I/O 작업에 최적화된 스레드 풀**에서 실행합니다.
- 네트워크 요청, 파일 읽기/쓰기, 데이터베이스 쿼리에 사용합니다.
- 스레드 수가 많이 확장될 수 있습니다 (기본 최대 64개).

```kotlin
withContext(Dispatchers.IO) {
    val result = api.fetchData() // 네트워크 호출
}
```

#### `Dispatchers.Default`

- **CPU 집약적인 작업**에 최적화된 스레드 풀에서 실행합니다.
- JSON 파싱, 정렬, 복잡한 계산, 리스트 변환 등에 사용합니다.
- CPU 코어 수에 맞춰 스레드 수를 제한합니다.

```kotlin
withContext(Dispatchers.Default) {
    val sorted = bigList.sortedBy { it.name } // CPU 작업
}
```

### 안드로이드 실무 패턴

```kotlin
// Repository 계층: IO에서 데이터 가져오기
suspend fun loadUser(): User = withContext(Dispatchers.IO) {
    api.getUser() // 네트워크
}

// ViewModel: Main에서 UI 업데이트
viewModelScope.launch {
    val user = loadUser() // IO 디스패처에서 실행됨
    _uiState.value = user // Main 스레드에서 실행
}
```

| Dispatcher | 용도 | 특징 |
|-----------|------|------|
| `Main` | UI 업데이트 | 메인 스레드 단 1개 |
| `IO` | 네트워크, 파일, DB | I/O 대기 많은 작업, 스레드 많음 |
| `Default` | CPU 계산, 파싱 | CPU 코어 수 기준 스레드 제한 |

---

## 4. 코루틴 빌더: `launch` vs `async`

코루틴을 **시작하는 방법**이 빌더(builder)입니다. 가장 많이 쓰는 두 가지를 구분합니다.

### `launch` — 결과가 필요 없을 때

- 코루틴을 시작하고 **`Job` 객체를 반환**합니다.
- 반환값이 없는 "실행하고 잊기(fire-and-forget)" 패턴에 사용합니다.

```kotlin
val job = viewModelScope.launch {
    saveToDatabase(data) // 결과를 기다릴 필요 없음
    updateUI()
}

job.cancel() // 취소 가능
```

### `async` — 결과가 필요할 때

- 코루틴을 시작하고 **`Deferred<T>` 객체를 반환**합니다.
- 실제 결과값은 `.await()`를 호출해 가져옵니다. (`await()`는 `suspend` 함수)

```kotlin
val deferred: Deferred<String> = viewModelScope.async {
    fetchDataFromNetwork() // String을 반환하는 suspend 함수
}

val result: String = deferred.await() // 결과가 준비될 때까지 일시 중단
```

### 병렬 처리 패턴

`async`의 진가는 **두 작업을 동시에 실행**할 때 발휘됩니다.

```kotlin
viewModelScope.launch {
    // 순차 실행: 총 2초 소요
    val user = fetchUser()    // 1초
    val posts = fetchPosts()  // 1초

    // 병렬 실행: 총 1초 소요
    val userDeferred = async { fetchUser() }   // 동시 시작
    val postsDeferred = async { fetchPosts() } // 동시 시작
    
    val user = userDeferred.await()
    val posts = postsDeferred.await()
    
    display(user, posts)
}
```

### 비교 요약

| | `launch` | `async` |
|--|---------|---------|
| 반환값 | `Job` | `Deferred<T>` |
| 결과 획득 | 없음 | `.await()` 호출 |
| 용도 | 부수 효과(저장, 업데이트) | 결과값이 필요한 계산 |
| 예외 처리 | 즉시 전파 | `.await()` 시점에 전파 |

---

## 코루틴 전체 흐름 예시

```kotlin
class MyViewModel : ViewModel() {

    fun loadData() {
        viewModelScope.launch(Dispatchers.Main) {  // UI 컨텍스트 시작
            
            val result = withContext(Dispatchers.IO) { // IO 스레드로 전환
                api.fetchData()                          // suspend: 일시 중단
            }                                            // Main 스레드로 복귀
            
            _liveData.value = result  // UI 업데이트
        }
    }
}
```

---

## 정리 요약

| 개념 | 핵심 포인트 |
|------|-----------|
| 코루틴 vs 스레드 | 코루틴은 스레드보다 가볍고 일시 중단/재개 가능 |
| `suspend` | CPS 변환으로 중단 지점을 저장; 스레드 블로킹 없이 대기 |
| `Dispatchers.Main` | UI 스레드; 블로킹 작업 금지 |
| `Dispatchers.IO` | 네트워크/파일/DB; I/O 대기가 많은 작업 |
| `Dispatchers.Default` | CPU 집약적 계산 |
| `launch` | 결과 없음, `Job` 반환, fire-and-forget |
| `async` | 결과 있음, `Deferred<T>` 반환, `.await()`로 수신 |
