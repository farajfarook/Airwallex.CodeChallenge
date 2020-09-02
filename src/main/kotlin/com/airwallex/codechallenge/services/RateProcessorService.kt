package com.airwallex.codechallenge.services

import com.airwallex.codechallenge.models.ConversionRate
import com.airwallex.codechallenge.rules.AlertEvent
import com.airwallex.codechallenge.rules.AlertRule

interface RateProcessorService {
    fun process(rate: ConversionRate): List<AlertEvent>
}

class RateProcessorServiceImpl(var rules: List<AlertRule>): RateProcessorService {
    override fun process(rate: ConversionRate): List<AlertEvent>
            = rules.mapNotNull { r -> r.perform(rate) }
}