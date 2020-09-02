package com.airwallex.codechallenge.rules.spot

import com.airwallex.codechallenge.rules.AlertEvent
import com.airwallex.codechallenge.rules.AlertType
import java.time.Instant

class SpotAlertEvent(timestamp: Instant, currencyPair: String)
    : AlertEvent(timestamp, currencyPair, AlertType.SpotChange)
