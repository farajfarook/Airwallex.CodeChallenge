package com.airwallex.codechallenge.models

import java.time.Instant

enum class RateChangeType {
    Raise,
    Fall,
    None
}

class RateChange(
    var type: RateChangeType = RateChangeType.None,
    var rate: Double = 0.0,
    var timestamp: Instant = Instant.MIN,
    var alertTimestamp: Instant = Instant.MIN)