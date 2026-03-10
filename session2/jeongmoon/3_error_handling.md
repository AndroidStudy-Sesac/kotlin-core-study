# 예외 처리와 안전한 자원 관리

<details>
<summary><h2> 🛡️ 1. 실전 예외처리와 자원 관리 기법</h2></summary>
<br/>

앱이 예기치 않게 죽는 것(Crash)을 막는 실전 방어 기법

### ① `runCatching`과 `Result`
자바의 지저분한 `try-catch` 대신 사용하는 코틀린의 우아한 에러 핸들링 방식
- 에러가 날 수 있는 코드를 `runCatching { }`으로 감싸 `Result` 객체로 반환
- 이후 `.onSuccess { }`, `.onFailure { }`를 체이닝해 직관적으로 정상/에러 흐름을 제어

---

### ② `use` 블록을 활용한 메모리 누수 방지
파일이나 DB 커넥션 등 `Closeable` 자원을 열었을 때 반드시 닫아주어야 하는(close) 작업을 자동화
- `.use { }` 블록을 사용하면, 정상적으로 블록이 끝나든 중간에 예외가 발생하든 <b>시스템이 알아서 안전하게 자원을 해제(`close()`)</b>해 줌
- 메모리 누수(Memory Leak)를 막음

</details>
