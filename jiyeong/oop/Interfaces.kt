package oop

fun main() {
    // 1. 인터페이스
    val child = Child()
    child.bar()
    child.foo()
}

interface MyInterface {
    val prop: Int // 추상 프로퍼티
    
    fun bar()
    fun foo() {
        // 본문이 있는 메서드 (선택적 오버라이딩)
        println("Default Implementation")
    }
}

class Child : MyInterface {
    override val prop: Int = 29
    
    override fun bar() {
        println("Child.bar() implementation")
    }
}
