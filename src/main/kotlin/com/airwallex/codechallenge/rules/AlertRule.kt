package com.airwallex.codechallenge.rules

import com.airwallex.codechallenge.models.ConversionRate

interface AlertRule {
    fun perform(rate: ConversionRate): AlertEvent?
}