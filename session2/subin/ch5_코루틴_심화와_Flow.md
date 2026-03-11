# 5장. Flow / 예외처리 / 취소 / 구조적 동시성

## 전체 핵심 요약

- **Flow**: 시간에 따라 값이 여러 번 변하는 데이터(상태/스트림)를 안전하게 다룸
- **예외 처리**: 어디서 try-catch 해야 하는지와 부모/자식 전파 규칙을 이해해야 앱이 안정적
- **취소**: Android에서는 lifecycle로 취소가 자동/명시적으로 일어나야 안전함
- **구조적 동시성**: 코루틴은 scope 안에서 시작되고 scope와 함께 끝나야 함(누수/좀비 작업 방지)

---

# 1) Flow (스트림/상태 관리)

## 핵심

- `suspend`는 **1회 결과**, `Flow`는 **N회 결과**
- Flow는 기본적으로 **cold** (collect해야 실행)
- 실무에선 `StateFlow`(상태), `SharedFlow`(이벤트)로 많이 씀

## 실무 포인트

- UI 상태: `StateFlow<UiState>`
- 일회성 이벤트: `SharedFlow<UiEvent>`
- lifecycle에 묶어서 collect (`repeatOnLifecycle`, `collectAsStateWithLifecycle`)

### 최소 예제: StateFlow로 UI 상태 관리

```kotlin
data class UiState(
    val isLoading: Boolean = false,
    val data: List<String> = emptyList(),
    val error: String? = null
)

class VM : ViewModel() {
    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    fun load() = viewModelScope.launch {
        _state.value = UiState(isLoading = true)
        val result = withContext(Dispatchers.IO) { fetchData() }
        _state.value = UiState(isLoading = false, data = result)
    }
}
```

---

# 2) 예외 처리 (Exception Handling)

## 핵심 체크

코루틴 예외 처리는 “try-catch만 쓰면 끝”이 아니라, 아래 규칙을 알아야 실무에서 안 터집니다.

- `launch`에서 발생한 예외는 **부모 scope로 전파**되어 취소를 유발할 수 있음
- `async`의 예외는 `await()` 시점에 **던져짐** (await 안 하면 늦게/안 보일 수 있음)
- `CoroutineExceptionHandler`는 **uncaught(잡히지 않은) 예외**에만 의미 있음
    
    (특히 `launch` 쪽에서)
    

---

## 2-1) 기본 원칙: 예외가 생길 수 있는 구간을 try-catch로 감싸라

```kotlin
viewModelScope.launch {
    try {
        val data = withContext(Dispatchers.IO) { fetchData() }
        render(data)
    } catch (e: Exception) {
        showError(e.message ?: "unknown")
    }
}
```

### 실무 포인트

- UI 상태 모델(`UiState.error`)로 에러를 흘리는 패턴이 가장 흔함

---

## 2-2) `async` 예외는 `await()`에서 터진다

```kotlin
viewModelScope.launch {
    val d = async(Dispatchers.IO) { fetchData() } // 여기서 바로 안 터질 수 있음
    try {
        val data = d.await() // 예외는 여기서 throw
        render(data)
    } catch (e: Exception) {
        showError("fail")
    }
}
```

---

## 2-3) `CoroutineExceptionHandler`

```kotlin
val handler = CoroutineExceptionHandler { _, throwable ->
    println("Unhandled: $throwable")
}

viewModelScope.launch(handler) {
    error("boom")
}
```

### 실무 포인트

- ViewModel에서 “전역적으로 잡겠다”보다는
    
    보통은 **각 use-case/요청 단위로 try-catch**가 더 예측 가능
    

---

## 2-4) `supervisorScope` / `SupervisorJob`

기본 코루틴은 자식 하나가 실패하면 부모/형제까지 취소될 수 있습니다.

“한 작업 실패가 전체를 죽이면 안 된다”면 supervisor가 필요합니다.

```kotlin
viewModelScope.launch {
    supervisorScope {
        val a = launch { taskA() } // 실패해도
        val b = launch { taskB() } // 다른 형제 작업이 계속될 수 있음
    }
}
```

### 실무 포인트

- “여러 API를 동시에 호출하는데, 하나 실패해도 나머지는 보여주고 싶다” 같은 요구에서 유용

---

# 3) 취소 (Cancellation)

## 핵심

- 코루틴은 기본적으로 **취소 가능**
- Android에서는 lifecycle이 바뀌면 취소가 일어나야 안전함
- 취소는 예외(`CancellationException`)로 표현되는 경우가 많음

---

## 3-1) lifecycle 기반 취소

- `viewModelScope`: ViewModel cleared 시 자동 취소
- `lifecycleScope`: lifecycle 종료 시 자동 취소
- Fragment에서는 `viewLifecycleOwner` 기준이 안전한 경우 많음

```kotlin
viewLifecycleOwner.lifecycleScope.launch {
    viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
        vm.state.collect { render(it) }
    }
}
```

---

## 3-2) 취소를 “협력적으로” 확인하는 키워드

긴 루프/큰 작업에서는 취소를 확인하거나 중단점을 가져야 합니다.

- `isActive`
- `ensureActive()`
- `yield()`

```kotlin
withContext(Dispatchers.Default) {
    for (i in 0 until 1_000_000) {
        ensureActive() // 취소되면 여기서 중단
        // heavy work...
    }
}
```

---

## 3-3) `try/finally`로 정리 작업 보장

취소되더라도 `finally`는 실행됩니다.

```kotlin
val job = viewModelScope.launch {
    try {
        doWork()
    } finally {
        releaseResources()
    }
}
```

---

# 4) 구조적 동시성 (Structured Concurrency)

## 핵심

코루틴은 “아무 데서나 막 실행”하면 안 되고 **scope 안에서 시작 → scope와 함께 끝나야** 합니다.

- 부모 코루틴이 자식 코루틴 생명주기를 관리
- “작업이 어디에 매달려 있는지”가 명확해짐
- 누수/좀비 작업 방지

---

## 4-1) `coroutineScope` vs `supervisorScope`

- `coroutineScope`: 자식 중 하나 실패하면 전체 취소될 수 있음 (기본 규칙)
- `supervisorScope`: 자식 실패가 다른 자식에 전파되지 않도록 완화

```kotlin
suspend fun loadAll(): List<String> = coroutineScope {
    val a = async { fetchA() }
    val b = async { fetchB() }
    listOf(a.await(), b.await())
}
```

### 실무 포인트

- “두 개 다 성공해야 화면을 그릴 수 있다” → `coroutineScope`가 자연스럽다
- “하나 실패해도 나머지는 보여주자” → `supervisorScope` 고려

---

## 4-2) 실무에서 구조적 동시성이 중요한 이유

- GlobalScope 같은 걸 쓰면:
    - lifecycle이 끝나도 작업이 계속 돌아감
    - 화면 없는 상태에서 UI 업데이트 시도 → 크래시
    - 디버깅 난이도 급상승

> ✅ 결론: “scope를 어디서 잡았는가”가 코루틴 품질을 좌우
> 

---
