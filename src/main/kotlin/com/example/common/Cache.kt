package com.example.common

class Cache<T>(private val maxSize: Int) {
    private var storage: MutableMap<String, T> = mutableMapOf()

    private var keys: MutableList<String> = mutableListOf()

    private fun removeExceedSize() {
        if (keys.size > maxSize) {
            val firstKey = keys[0]

            storage.remove(firstKey)
            keys.removeAt(0)
        }
    }

    fun store(key: String, data: T) {
        storage[key] = data
        keys.add(key)

        removeExceedSize()
    }

    fun getByKey(key: String): T? {
        return storage[key]
    }
}