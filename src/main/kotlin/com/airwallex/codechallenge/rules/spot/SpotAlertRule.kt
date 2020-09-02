package com.airwallex.codechallenge.rules.spot

import com.airwallex.codechallenge.models.ConversionRate
import com.airwallex.codechallenge.rules.AlertEvent
import com.airwallex.codechallenge.rules.AlertRule
import com.airwallex.codechallenge.services.RateHistoryService
import java.time.Duration
import kotlin.math.abs

class SpotAlertRule(var historyService: RateHistoryService) :
    AlertRule {

    private val changePercentage = 10
    private val historyHoldDuration = Duration.ofMinutes(5)

    override fun perform(rate: ConversionRate): AlertEvent? {

        historyService.recycle(rate.currencyPair, historyHoldDuration)
        val rates = historyService.getRates(rate.currencyPair)
        historyService.update(rate)

        val avgRate = rates.average()
        val change = abs(rate.rate - avgRate)
        val percentChange = (change / avgRate) * 100

        return if(percentChange >= changePercentage)
            SpotAlertEvent(rate.timestamp, rate.currencyPair)
        else null
    }
}
