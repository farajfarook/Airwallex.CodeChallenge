package com.airwallex.codechallenge.cache

import com.airwallex.codechallenge.models.RateChange
import com.airwallex.codechallenge.models.RateChangeType
import com.airwallex.codechallenge.models.RateHistory
import java.lang.Exception
import java.time.Instant

interface RateCacheService {
    fun getHistory(currencyPair: String): RateHistory
    fun setHistory(currencyPair: String, history: RateHistory)

    fun getChange(currencyPair: String): RateChange
    fun setChange(currencyPair: String, change: RateChange)
}

class RateCacheServiceImpl(var cache: CacheService) :RateCacheService{

    private val historyPrefix = "history"
    private val changePrefix = "change"

    override fun getHistory(currencyPair: String): RateHistory {
        if(currencyPair.isEmpty()) throw InvalidCurrencyPairException()
        var cache = cache.get<RateHistory>(cacheKey(historyPrefix, currencyPair))
        return when(cache) {
            null -> {
                cache = RateHistory()
                setHistory(currencyPair, cache)
                return cache
            }
            else -> cache
        }
    }

    override fun setHistory(currencyPair: String, history: RateHistory) {
        if(currencyPair.isEmpty()) throw InvalidCurrencyPairException()
        cache.set(cacheKey(historyPrefix, currencyPair), history)
    }

    override fun getChange(currencyPair: String): RateChange {
        if(currencyPair.isEmpty()) throw InvalidCurrencyPairException()
        var cache = cache.get<RateChange>(cacheKey(changePrefix, currencyPair))
        return when(cache) {
            null -> {
                cache = RateChange()
                setChange(currencyPair, cache)
                return cache
            }
            else -> cache
        }
    }

    override fun setChange(currencyPair: String, change: RateChange) {
        if(currencyPair.isEmpty()) throw InvalidCurrencyPairException()
        cache.set(cacheKey(changePrefix, currencyPair), change)
    }

    private fun cacheKey(domain:String, currencyPair: String) = "${domain}_${currencyPair}"
}

class InvalidCurrencyPairException: Exception()