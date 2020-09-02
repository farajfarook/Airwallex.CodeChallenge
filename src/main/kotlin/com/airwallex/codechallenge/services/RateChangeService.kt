package com.airwallex.codechallenge.services

import com.airwallex.codechallenge.cache.RateCacheService
import com.airwallex.codechallenge.models.ConversionRate
import com.airwallex.codechallenge.models.RateChange
import com.airwallex.codechallenge.models.RateChangeType
import java.time.Duration
import java.time.Instant

interface RateChangeService {
    fun getChangeType(rate: ConversionRate): RateChangeType
    fun getCurrentChangeType(currencyPair: String): RateChangeType
    fun getCurrentChangeTime(currencyPair: String): Instant
    fun updateAlertTimer(currencyPair: String, timestamp: Instant)
    fun isTimeToAlert(currencyPair: String, rateTime: Instant, alertAfter: Duration, detectChangeAfter: Duration): Boolean
    fun resetTo(rate: ConversionRate, changeType: RateChangeType)
}

class RateChangeServiceImpl(var cache: RateCacheService) : RateChangeService {

    override fun getChangeType(rate: ConversionRate): RateChangeType {
        val change = cache.getChange(rate.currencyPair)
        return when {
            change.rate > rate.rate -> RateChangeType.Fall
            change.rate < rate.rate -> RateChangeType.Raise
            else -> RateChangeType.None
        }
    }

    override fun getCurrentChangeType(currencyPair: String): RateChangeType = cache.getChange(currencyPair).type

    override fun getCurrentChangeTime(currencyPair: String): Instant = cache.getChange(currencyPair).timestamp

    override fun isTimeToAlert(currencyPair: String, rateTime: Instant, alertAfter: Duration, detectChangeAfter: Duration): Boolean {
        val cache = cache.getChange(currencyPair)

        val validateTimePlusThreshold = cache.timestamp.plusSeconds(detectChangeAfter.seconds)
        val alertTimePlusThreshold = cache.alertTimestamp.plusSeconds(alertAfter.seconds)

        val validateThreshold = !validateTimePlusThreshold.isAfter(rateTime)
        val alertThreshold = !alertTimePlusThreshold.isAfter(rateTime)
        return validateThreshold && alertThreshold
    }

    override fun resetTo(rate: ConversionRate, changeType: RateChangeType) {
        cache.setChange(rate.currencyPair, RateChange(
            type = changeType,
            timestamp = rate.timestamp,
            rate = rate.rate,
            alertTimestamp = Instant.MIN
        ))
    }

    override fun updateAlertTimer(currencyPair: String, timestamp: Instant) {
        val change = cache.getChange(currencyPair)
        change.alertTimestamp = timestamp
        cache.setChange(currencyPair, change)
    }
}