package variable

fun main() {
    // 1. lateinit
    val activity = MyActivity()
    activity.init()
    activity.checkInit()

    // 2. lazy
    val lazyValue : String by lazy {
        println("초기화 수행")
        "Heavy Result" // 블록의 마지막 줄인 "Heavy Result"가 lazyValue의 값으로 결정되고 저장(캐싱)됩니다.
    }
    println(lazyValue) // "초기화 수행" 출력 후 "Heavy Result" 출력
    println(lazyValue) // "Heavy Result"만 출력
}

class MyActivity {
    lateinit var adapter: String // 예시를 위해 String으로 대체

    fun init() {
        adapter = "MyAdapter"
    }

    fun checkInit() {
        if (::adapter.isInitialized) {
            println("Initialized: $adapter")
        } else {
            println("Not initialized")
        }
    }
}