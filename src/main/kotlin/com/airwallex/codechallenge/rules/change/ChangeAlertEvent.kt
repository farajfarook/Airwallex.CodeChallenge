package com.airwallex.codechallenge.rules.change

import com.airwallex.codechallenge.rules.AlertEvent
import com.airwallex.codechallenge.rules.AlertType
import java.time.Instant

class ChangeAlertEvent(
    timestamp: Instant,
    currencyPair: String,
    alert: AlertType,
    var seconds: Long
) : AlertEvent(timestamp, currencyPair, alert)