package com.god.seep.weather.entity


data class Person(var name: String, var age: Int)

class Boy {
    var name: String = ""
        get() = field.toUpperCase()
    var age: Int = 0
        set(value) {
            field = Math.abs(value)
        }
}