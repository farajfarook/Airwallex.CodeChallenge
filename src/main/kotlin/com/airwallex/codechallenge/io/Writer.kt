package com.airwallex.codechallenge.io

import com.airwallex.codechallenge.rules.AlertEvent
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper

class Writer(var json: ObjectMapper) {

    fun write(event: AlertEvent) {
        try {
            println(json.writeValueAsString(event))
        } catch (ex: JsonProcessingException) {
            println("Error parsing the event: $ex")
        }
    }
}