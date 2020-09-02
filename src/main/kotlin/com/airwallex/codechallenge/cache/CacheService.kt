package com.airwallex.codechallenge.cache

interface CacheService {
    fun <T> set(key: String, value: T)
    fun <T> get(key: String) : T?
}

class CacheServiceImpl : CacheService {

    private var map = mutableMapOf<String, Any>()

    override fun <T> set(key: String, value: T) {
        if(key.isEmpty()) throw InvalidCacheKeyException("Key cannot be empty")
        map[key] = value as Any
    }

    override fun <T> get(key: String): T? {
        return try {
            @Suppress("UNCHECKED_CAST")
            map[key] as T
        } catch (ex: Exception){
            null
        }
    }
}

class InvalidCacheKeyException(msg: String): Exception(msg)