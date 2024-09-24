package com.example.common.cache

object NicknameCache {
    private val collection = "nickname"

    suspend fun store(nickname: String) {
        Cache.sAdd(collection, nickname)
    }

    suspend fun exists(nickname: String): Boolean {
        return Cache.sCheckExists(collection, nickname) ?: false
    }
}