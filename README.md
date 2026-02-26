# kotlin-core-study
코틀린 스터디

## 사용방법
- session1 or 2/이름 형식으로 브랜치를 만듭니다.
- 만든 브랜치 안에 이름으로 폴더를 만듭니다.
- 각 회차별 주제별로 md 파일 형식으로 공부한걸 정리합니다. ex. 코루틴과 비동기에 관하여.md, 인터페이스와 상속.md
- 스터디 시간에 질문 및 피드백 시간을 가집니다. 

## 🗓️ 1회차: 코틀린 코어 문법과 객체지향 심층 해부

안드로이드 프레임워크의 뼈대가 되는 객체지향 개념과, 코틀린만의 안전하고 간결한 문법을 다룹니다.

### 1. 변수, 타입, 그리고 안전한 초기화

데이터를 어떻게 선언하고, 언제 초기화하며, Null을 어떻게 방어할 것인가에 집중합니다.

- [ ]  **변수와 프로퍼티:** `val` vs `var`, 백킹 필드(Backing Field)와 커스텀 Getter/Setter
- [ ]  **코틀린의 타입 계층 :** `Any` (모든 객체의 조상), `Unit` (자바의 void와 차이), `Nothing` (정상 종료되지 않음)
- [ ]  **Null 안전성:** `?`(Nullable), `?:`(엘비스), `?.`(안전한 호출), `!!`(널 단언)
- [ ]  **스마트 캐스트와 타입 검사:** `is` / `!is`, 안전한 형변환 `as?` (Implicit Cast 포함)
- [ ]  **지연 초기화 전략 (면접 단골):** `lateinit` (var) vs `lazy` (val)의 차이점과 안드로이드 실무 활용

### 2. 제어 흐름

- [ ]  **식(Expression)으로서의 제어문:** `if`와 `when`이 값을 반환한다는 것의 의미
- [ ]  **실무 압축 `for`문 활용 (3가지 필수 관용구):**
    - 단순 아이템 순회: `for (item in list)`
    - 인덱스와 함께 순회: `for ((index, item) in list.withIndex())` (리사이클러뷰 등에서 활용)
    - 단순 반복 작업: `repeat(n) { ... }`

### 3. 객체지향: 상속과 인터페이스

- [ ]  **클래스와 가시성:** 기본 `final` 및 `public` 특징, 멀티 모듈에서 중요한 `internal`의 정확한 범위
- [ ]  **초기화(Initialization) 순서:**
- [ ]  **상속 (Inheritance):**
- [ ]  **인터페이스 (Interface)**

### 4. 코틀린 클래스와 객체

- [ ]  **`data class`:** DTO 역할, 자동 생성되는 메서드들(`equals`, `hashCode`, `toString`, `copy`, `componentN`)의 원리와 활용
- [ ]  **`sealed class` / `sealed interface`:** 상태 관리(로딩/성공/실패 등)에 필수적인 이유, 컴파일러가 자식 클래스를 완벽히 인지하여 `when`문에서 얻는 이점
- [ ]  **`object`와 `companion object`:** 싱글톤 패턴과 팩토리 메서드 패턴 구현 방법
- [ ]  Enum vs Sealed 비교
- [ ]  inner
- [ ]  anonymous class
- [ ]  init
- [ ]  Access Modifier