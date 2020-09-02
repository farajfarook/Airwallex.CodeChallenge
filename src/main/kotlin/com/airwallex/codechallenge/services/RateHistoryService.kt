package com.airwallex.codechallenge.services

import com.airwallex.codechallenge.cache.RateCacheService
import com.airwallex.codechallenge.models.ConversionRate
import com.airwallex.codechallenge.models.RateHistoryEntry
import java.time.Duration
import java.time.Instant

interface RateHistoryService {
    fun update(rate: ConversionRate)
    fun recycle(currencyPair: String, validDuration: Duration)
    fun getRates(currencyPair: String): List<Double>
}

class RateHistoryServiceImpl(var cache: RateCacheService) : RateHistoryService{

    override fun recycle(currencyPair: String, validDuration: Duration) {
        val validUntil = Instant.now().plusSeconds(validDuration.seconds)
        val history = cache.getHistory(currencyPair)
        val filteredRates = history.rates.filter { r -> r.timestamp.isBefore(validUntil) }
        history.rates = filteredRates
        cache.setHistory(currencyPair, history)
    }

    override fun update(rate: ConversionRate) {
        val history = cache.getHistory(rate.currencyPair)
        val rates = history.rates.toMutableList()
        rates.add(RateHistoryEntry(rate.timestamp, rate.rate))
        history.rates = rates.toList()
        cache.setHistory(rate.currencyPair, history)
    }

    override fun getRates(currencyPair: String):  List<Double> {
        val history = cache.getHistory(currencyPair)
        return history.rates.map { r -> r.rate }
    }
}