package com.airwallex.codechallenge.rules

import com.fasterxml.jackson.annotation.JsonValue
import java.time.Instant

open class AlertEvent (
    var timestamp: Instant,
    var currencyPair: String,
    val alert: AlertType
)


enum class AlertType(
    @JsonValue var value: String) {

    SpotChange("spotChange"),
    Raising("raising"),
    Falling("falling")
}