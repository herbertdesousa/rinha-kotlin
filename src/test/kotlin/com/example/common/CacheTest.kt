package com.example.common

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

data class Item(val label: String, val amount: Int)

class CacheTest {
    @Test
    fun store() {
        val cache = Cache<Item>(3)

        val item1 = Item("Short", 3)
        cache.store("item1", item1)

        assertEquals(cache.getByKey("non-existing-item"), null)

        assertEquals(cache.getByKey("item1"), item1)

        cache.store("item2", Item("Shirt", 2))
        cache.store("item3", Item("Cap", 4))
        cache.store("item4", Item("Hat", 1))

        assertEquals(cache.getByKey("item1"), null)

        cache.store("item5", Item("Blouse", 2))

        assertEquals(cache.getByKey("item2"), null)
    }
}