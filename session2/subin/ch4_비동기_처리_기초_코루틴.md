# 4장. 비동기 처리 기초 (코루틴 - Coroutines)

## 전체 핵심 요약

- 코루틴은 스레드가 아니라 **경량 작업 단위**(suspendable computation)이고 스레드 위에서 실행된다
    - 코루틴은 **스레드가 아니다.**
    - 코루틴은 **중단(suspend)과 재개(resume)가 가능한 작업 단위**
    - 실제 실행은 **스레드(Dispatchers가 제공하는 스레드/스레드풀) 위에서** 이뤄진다.
    - 그래서 경량 스레드(lightweight thread)라는 표현을 **비유로** 많이 쓰지만 **OS 스레드와 동급이라는 뜻은 아니다.**
    - **경량 작업 단위**란? : OS 스레드가 아니라, **중단(suspend)·재개(resume) 가능한 실행 흐름(상태+다음 실행 지점)**을 객체처럼 관리하는 단위
- `suspend`는 스레드를 막지 않고 **일시 중단/재개**가 가능한 함수라는 의미
- `Dispatchers`는 어디서 실행할지를 정하는 스레드/스케줄링 정책
    - Android 실무 기본 분리: `Main`(UI) / `IO`(네트워크·DB) / `Default`(CPU 작업)
- 코루틴 빌더:
    - `launch` : 결과 반환 없음 (Job)
    - `async` : 결과 반환 있음 (Deferred)
- Android에서는 **lifecycle에 묶어서** 코루틴을 실행/취소해야 안전하다 (키워드: `viewModelScope`, `lifecycleScope`)

> ✅ 이번 장 범위
> 
> - 코루틴 개념(스레드와 차이)
> - `suspend` 내부 동작 감각
> - Dispatcher 종류와 실무 분리
> - `launch` vs `async`

---

# 1) 코루틴 개념: 스레드와의 차이

## 핵심

- **Thread**: OS가 관리하는 실행 단위 (비용 큼)
- **Coroutine**: Kotlin 런타임이 관리하는 작업 단위 (비용 작음)
- 코루틴은 “스레드 없이 동작”하는 게 아니라, **스레드 위에서 실행되되 더 싸게 여러 개를 운영**하는 방식

## 왜 코루틴이 유리한가? (실무 포인트)

- 스레드를 많이 만들면 비용(메모리, 컨텍스트 스위칭)이 큼
- 코루틴은 수많은 작업을 **적은 스레드 풀**에서 효율적으로 처리 가능
- Android에서 네트워크/DB/타이머/백그라운드 작업을 표준화된 방식으로 처리

> ✅ 한 줄 정리
> 
> 
> 코루틴 = “스레드를 대체”가 아니라 **스레드를 효율적으로 쓰는 추상화**
> 

---

# 2) `suspend` 키워드: 일시 중단과 재개

## 핵심

`suspend fun`은 실행 도중 **중단(suspend)** 할 수 있고, 나중에 **재개(resume)** 될 수 있는 함수입니다.

- 중요한 점: **스레드를 블로킹하지 않는다**
- 즉, 기다리는 동안 스레드를 점유하지 않고 다른 작업을 실행할 수 있음

```kotlin
suspend fun fetchUserName(): String {
    // 네트워크 호출 같은 suspend 함수가 들어온다고 가정
    return "Heewon"
}
```

## 내부 동작 감각(면접/실무 공통 포인트)

- `suspend`는 “중간에 멈출 수 있는 상태 머신(state machine)”로 컴파일됨
- 멈췄을 때 현재 위치/로컬 변수/다음 실행 지점을 저장해두고,
- 나중에 다시 이어서 실행함

> ✅ 너무 깊게 들어갈 필요는 없고,
> 
> 
> “블로킹이 아니라 suspend + resume이다” 이 감각이 핵심
> 

---

# 3) 코루틴 스레드 풀: `Dispatchers` 종류와 실무 분리

## 핵심

Dispatcher는 코루틴을 **어떤 스레드(또는 스레드 풀)에서 실행할지** 결정합니다.

- `Dispatchers.Main` : UI 스레드 (Android Main Thread)
- `Dispatchers.IO` : I/O 작업 (네트워크, DB, 파일)
- `Dispatchers.Default` : CPU 작업 (정렬, 암호화, 파싱, 이미지 처리 등)

---

## 3-1) Android 실무 분리 기준

### `Main`

- UI 업데이트
- View 상태 변경
- Compose state update

### `IO`

- 네트워크 요청 (Retrofit)
- DB 작업 (Room)
- 파일/디스크 I/O

### `Default`

- JSON 파싱(큰 데이터)
- 이미지/텍스트 처리
- 정렬/집계/알고리즘성 작업

---

## 3-2) `withContext`로 Dispatcher 전환

실무에서 가장 기본적인 패턴:

```kotlin
suspend fun loadUser(): User {
    val user = withContext(Dispatchers.IO) {
        // 네트워크/DB 작업
        fetchUserFromApi()
    }

    return user
}
```

### 실무 포인트

- UI에서 호출해도 내부에서 IO로 넘기면 Main 스레드 블로킹을 피할 수 있음
- 필요한 시점에만 전환하되, 전환 남발은 가독성을 해칠 수 있음

---

# 4) 코루틴 빌더: `launch` vs `async`

## 4-1) `launch`

- 결과 반환 없음
- 반환 타입: `Job`
- “실행시키고 끝”인 작업에 사용

```kotlin
val job = scope.launch {
    saveLog()
}
```

### 실무에서 자주 쓰는 곳

- UI 이벤트 처리 후 백그라운드 작업
- 데이터 저장, 로그 전송
- fire-and-forget 작업

---

## 4-2) `async`

- 결과 반환 있음
- 반환 타입: `Deferred<T>`
- `await()`로 결과를 받음

```kotlin
val deferred = scope.async {
    fetchUserName()
}

val name = deferred.await()
```

### 실무에서 자주 쓰는 곳

- 병렬로 여러 요청을 날리고 결과를 합칠 때
- 여러 데이터 소스를 동시에 가져올 때

---

## 4-3) `launch` vs `async` 한 줄 정리

- `launch`: “결과 필요 없음” → Job
- `async`: “결과 필요함” → Deferred + await

> ✅ 실무 팁
> 
> 
> `async`는 남발하면 구조가 복잡해질 수 있으니
> 
> “병렬 실행 + 결과 합치기”가 확실할 때만 쓰는 게 좋음
> 

---

# 5) Android 실무 예제 (필수 패턴)

## 5-1) ViewModel에서 `viewModelScope` 사용

- ViewModel이 사라질 때 자동 취소되어 안전함

```kotlin
class ProfileViewModel : ViewModel() {

    fun loadProfile() {
        viewModelScope.launch {
            val user = withContext(Dispatchers.IO) {
                fetchUserFromApi()
            }
            // UI 상태 업데이트 (Main)
            updateUi(user)
        }
    }
}
```

### 실무 포인트

- Android에서는 “어디서 scope를 잡는가”가 매우 중요
- `GlobalScope` 같은 건 지양 (누수/취소 관리 어려움)

---

## 5-2) Activity/Fragment에서 `lifecycleScope`

```kotlin
class ProfileFragment : Fragment() {

    fun load() {
        viewLifecycleOwner.lifecycleScope.launch {
            val data = withContext(Dispatchers.IO) { fetchData() }
            render(data)
        }
    }
}
```

### 실무 포인트

- Fragment는 `viewLifecycleOwner`를 쓰는 게 안전한 경우가 많음
- 화면이 사라지면 자동 취소되어 불필요한 작업/크래시 방지

---

# 6) 코루틴 기초 키워드

## 6-1) 취소(Cancellation)

- 코루틴은 기본적으로 **취소 가능**
- lifecycle scope가 중요한 이유도 “취소를 자동으로 해주기 때문”

키워드:

- `isActive`
- `CancellationException`

---

## 6-2) 예외 처리

- `try-catch-finally` (suspend에서도 동일)
- `runCatching`
- `CoroutineExceptionHandler`
- `supervisorScope` / `SupervisorJob`

---

## 6-3) 구조적 동시성(Structured Concurrency)

- 코루틴은 scope 내부에서 시작되고 scope와 생명주기를 같이함
- “부모-자식 코루틴 관계”가 기본

키워드:

- parent/child coroutine
- `coroutineScope { }`
- `supervisorScope { }`

---

## 6-4) Flow / Channel / select

- `Flow`: 비동기 스트림 (map/filter 등 컬렉션 연산과 연결)
- `Channel`: 코루틴 간 통신
- `select`: 여러 suspend 중 먼저 완료되는 것 선택

---

# 7) 최종 정리

- 코루틴은 스레드를 대체하는 게 아니라, **스레드를 효율적으로 쓰는 비동기 모델**
- `suspend`는 “블로킹이 아니라 suspend/resume”을 의미
- Dispatcher 분리는 실무에서 기본:
    - UI는 `Main`, 네트워크/DB는 `IO`, CPU 작업은 `Default`
- `launch`는 fire-and-forget, `async`는 결과 필요할 때 (`await`)
- Android에서는 scope를 lifecycle에 묶어야 안전:
    - `viewModelScope`, `lifecycleScope`

---
