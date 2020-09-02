package com.airwallex.codechallenge.rules.change

import com.airwallex.codechallenge.models.ConversionRate
import com.airwallex.codechallenge.models.RateChangeType
import com.airwallex.codechallenge.rules.AlertEvent
import com.airwallex.codechallenge.rules.AlertRule
import com.airwallex.codechallenge.rules.AlertType
import com.airwallex.codechallenge.services.RateChangeService
import java.time.Duration
import java.time.temporal.ChronoUnit

class ChangeAlertRule(var changeService: RateChangeService) : AlertRule {

    private val alertTimeAfter: Duration = Duration.ofMinutes(1)
    private val detectChangeAfter: Duration = Duration.ofMinutes(15)

    override fun perform(rate: ConversionRate): AlertEvent? {
        val changeType = changeService.getChangeType(rate)
        val currentChangeType = changeService.getCurrentChangeType(rate.currencyPair)
        val currentChangeTime = changeService.getCurrentChangeTime(rate.currencyPair)

        val shouldAlert = when(changeType) {
            currentChangeType -> changeService.isTimeToAlert(rate.currencyPair, rate.timestamp, alertTimeAfter, detectChangeAfter)
            else -> {
                changeService.resetTo(rate, changeType)
                false
            }
        }

        if (!shouldAlert) return null

        changeService.updateAlertTimer(rate.currencyPair, rate.timestamp)
        return ChangeAlertEvent(
            timestamp = rate.timestamp,
            currencyPair = rate.currencyPair,
            alert = when(changeType) { RateChangeType.Raise -> AlertType.Raising else -> AlertType.Falling},
            seconds = currentChangeTime.until(rate.timestamp, ChronoUnit.SECONDS) + 1
        )
    }
}