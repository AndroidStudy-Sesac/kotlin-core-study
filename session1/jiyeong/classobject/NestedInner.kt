package classobject

fun main() {
    // 1. Nested Class
    val nested = Outer.Nested()
    println(nested.foo())

    // 2. Inner Class
    val outer = Outer()
    val inner = outer.Inner()
    println(inner.foo())
}

class Outer {
    private val bar: Int = 1
    
    class Nested {
        fun foo() = 2
    }
    
    inner class Inner {
        fun foo() = bar // 외부 클래스 멤버 접근 가능
    }
}
