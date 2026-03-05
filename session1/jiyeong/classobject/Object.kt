package classobject

fun main() {
    // 1. object (Singleton)
    DataProvider.register()

    // 2. companion object
    val instance = MyClass.create()
}

object DataProvider {
    fun register() { println("DataProvider registered") }
}

class MyClass {
    companion object Factory {
        fun create(): MyClass = MyClass()
    }
}
