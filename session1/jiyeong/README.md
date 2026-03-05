## 🗓️ 1회차: 코틀린 코어 문법과 객체지향 심층 해부

안드로이드 프레임워크의 뼈대가 되는 객체지향 개념과, 코틀린만의 안전하고 간결한 문법을 다룹니다.

### 1. 변수, 타입, 그리고 안전한 초기화

데이터를 어떻게 선언하고, 언제 초기화하며, Null을 어떻게 방어할 것인가에 집중합니다.

- [x]  **변수와 프로퍼티:** `val` vs `var`, 백킹 필드(Backing Field)와 커스텀 Getter/Setter
- [x]  **코틀린의 타입 계층 :** `Any` (모든 객체의 조상), `Unit` (자바의 void와 차이), `Nothing` (정상 종료되지 않음)
- [x]  **Null 안전성:** `?`(Nullable), `?:`(엘비스), `?.`(안전한 호출), `!!`(널 단언)
- [x]  **스마트 캐스트와 타입 검사:** `is` / `!is`, 안전한 형변환 `as?` (Implicit Cast 포함)
- [x]  **지연 초기화 전략 (면접 단골):** `lateinit` (var) vs `lazy` (val)의 차이점과 안드로이드 실무 활용

### 2. 제어 흐름

- [x]  **식(Expression)으로서의 제어문:** `if`와 `when`이 값을 반환한다는 것의 의미
- [x]  **실무 압축 `for`문 활용 (3가지 필수 관용구):**
    - 단순 아이템 순회: `for (item in list)`
    - 인덱스와 함께 순회: `for ((index, item) in list.withIndex())` (리사이클러뷰 등에서 활용)
    - 단순 반복 작업: `repeat(n) { ... }`
- [x]  **예외 처리:** `try-catch`도 식(Expression)이다.

### 3. 객체지향: 상속과 인터페이스

- [x]  **클래스와 가시성:** 기본 `final` 및 `public` 특징, 멀티 모듈에서 중요한 `internal`의 정확한 범위
- [x]  **초기화(Initialization) 순서:**
- [x]  **상속 (Inheritance):**
- [x]  **인터페이스 (Interface)**

### 4. 코틀린 클래스와 객체

- [x]  **`data class`:** DTO 역할, 자동 생성되는 메서드들(`equals`, `hashCode`, `toString`, `copy`, `componentN`)의 원리와 활용
- [x]  **`sealed class` / `sealed interface`:** 상태 관리(로딩/성공/실패 등)에 필수적인 이유, 컴파일러가 자식 클래스를 완벽히 인지하여 `when`문에서 얻는 이점
- [x]  **`object`와 `companion object`:** 싱글톤 패턴과 팩토리 메서드 패턴 구현 방법
- [x]  Enum vs Sealed 비교
- [x]  inner
- [x]  anonymous class
- [x]  init
- [x]  Access Modifier
- [x]  **함수의 유연성:** `Default Arguments`(기본 인자)와 `Named Arguments`(지명 인자)
- [x]  **확장 함수 (Extension Function):** 상속 없이 클래스 기능 확장하기 (OCP 원칙 준수)