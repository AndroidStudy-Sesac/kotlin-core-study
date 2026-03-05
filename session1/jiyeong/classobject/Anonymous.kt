package classobject

fun main() {
    // 1. Anonymous Class
    val listener = object : MouseListener {
        override fun onClick() {
            println("Clicked!")
        }
    }
    listener.onClick()
}

interface MouseListener {
    fun onClick()
}
