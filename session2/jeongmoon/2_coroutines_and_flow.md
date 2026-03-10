# 비동기 처리와 반응형 스트림

<details>
<summary><h2> 🧵 1. 비동기 처리 기초 (코루틴)</h2></summary>
<br/>

### ① 스레드 vs 코루틴

| 구분 | 특징 |
| --- | --- |
| **Thread** | 무겁고 Context Switching 비용이 큼 |
| **Coroutine (경량 스레드)** | Context Switching 비용이 거의 0에 가깝고, 하나의 스레드에서 수천 개 실행 가능 |

---

### ② suspend 키워드 (일시 중단과 재개)
코루틴 안에서만 실행될 수 있는 일시 중단 함수
- 스레드를 차단(Block)하지 않음
- 작업 중 <b>일시 중단(Suspend)</b>하고 다른 코루틴에 자원을 양보함
- 작업 완료 시 멈췄던 곳부터 <b>재개(Resume)</b>하는 Non-blocking 방식

---

### ③ Dispatchers (스레드 풀)

| Dispatcher | 역할 및 실무 사용처 |
| --- | --- |
| **`Dispatchers.Main`** | 메인(UI) 스레드 작업. 화면 업데이트, 토스트 띄우기, 클릭 이벤트 |
| **`Dispatchers.IO`** | 입출력 작업. 서버 통신(Retrofit API), 로컬 DB(Room) 접근, 파일 읽기/쓰기 |
| **`Dispatchers.Default`** | 무거운 CPU 연산. 리스트 정렬, 대용량 JSON 파싱, 이미지 필터 적용 |

---

### ④ 코루틴 빌더
- **`launch` (Fire & Forget):** 결과 반환이 필요 없을 때 사용. `Job` 객체를 반환하며, 에러 발생 시 앱이 죽을 수 있어 예외 처리에 주의!
- **`async` (결과 반환 ⭕):** 연산이나 통신 결과값이 필요할 때 사용. `Deferred` 객체를 반환하며, `.await()`를 호출해 결과가 올 때까지 기다림.(API 병렬 호출 시 필수)
- **`withContext`:** 실행 중인 코루틴의 스레드(Dispatcher)만 잠깐 바꿀 때 사용.

> **🚨 `runBlocking` 금지**
>
> 코루틴이 끝날 때까지 현재 스레드를 완전히 멈춤(Block).
>
> UI 스레드에서 쓰면 앱이 먹통(ANR)이 되니 테스트용으로만 사용.

</details>

<details>
<summary><h2> 🌊 2. 반응형 비동기 스트림 (Flow & Channel)</h2></summary>
<br/>

단일 결과값이 아니라, 데이터베이스 업데이트처럼 <b>'지속적으로 여러 개의 데이터'</b>를 비동기로 받아야 할 때 사용

- **`Flow` (Cold Stream):** 누군가 수집(`collect`)하기 전까지는 꼼짝도 하지 않는 게으른(Cold) 스트림. 안드로이드 MVVM 아키텍처 관찰자 패턴의 핵심
- **`Channel` (Hot Stream):** 코루틴 간에 데이터를 주고받는 파이프라인(큐) 역할. 받는 사람이 없어도 일단 데이터를 발행(Hot).

</details>
