package com.god.seep.weather.entity


data class Person(var name: String, var age: Int)

class Boy constructor(name: String, age: Int = 0) {
    var name: String = ""
        get() = field.toUpperCase()
    var age: Int = 0
        set(value) {
            field = Math.abs(value)
        }

    init {
        this.name = name
        this.age = age
    }
}

data class Entity<T>(val success: Boolean, var data: T, val error: String)