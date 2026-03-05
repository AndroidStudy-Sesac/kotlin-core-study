package oop

fun main() {
    // 1. 상속
    val circle = Circle()
    circle.draw()
    circle.fill()
}

open class Shape {
    open fun draw() { println("Shape.draw()") }
    fun fill() { println("Shape.fill()") } // 오버라이딩 불가
}

class Circle : Shape() {
    override fun draw() { println("Circle.draw()") }
}
