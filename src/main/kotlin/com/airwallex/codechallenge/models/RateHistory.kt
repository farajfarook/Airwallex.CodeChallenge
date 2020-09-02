package com.airwallex.codechallenge.models

import java.time.Instant

class RateHistory (
    var rates: List<RateHistoryEntry> = emptyList()
)

class RateHistoryEntry (var timestamp: Instant, var rate: Double)
