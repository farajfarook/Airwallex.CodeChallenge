package com.airwallex.codechallenge.models

import java.time.Instant

data class ConversionRate (
    val timestamp: Instant,
    val currencyPair: String,
    val rate: Double
)