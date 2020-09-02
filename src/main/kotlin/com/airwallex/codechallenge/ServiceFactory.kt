package com.airwallex.codechallenge

import com.airwallex.codechallenge.cache.CacheService
import com.airwallex.codechallenge.cache.CacheServiceImpl
import com.airwallex.codechallenge.cache.RateCacheService
import com.airwallex.codechallenge.cache.RateCacheServiceImpl
import com.airwallex.codechallenge.io.Reader
import com.airwallex.codechallenge.io.Writer
import com.airwallex.codechallenge.rules.spot.SpotAlertRule
import com.airwallex.codechallenge.rules.change.ChangeAlertRule
import com.airwallex.codechallenge.rules.AlertRule
import com.airwallex.codechallenge.services.*
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule

object ServiceFactory {
    var reader: Reader = Reader()
    var cache: CacheService = CacheServiceImpl()
    var rateCache: RateCacheService = RateCacheServiceImpl(cache)
    var change: RateChangeService = RateChangeServiceImpl(rateCache)
    var history: RateHistoryService = RateHistoryServiceImpl(rateCache)
    var rules: List<AlertRule> = listOf(
        SpotAlertRule(history),
        ChangeAlertRule(change)
    )
    var processor: RateProcessorService = RateProcessorServiceImpl(rules)
    var json: ObjectMapper = ObjectMapper().apply {
        val module = JavaTimeModule()
        registerModule(module)
    }
    var writer: Writer = Writer(json)
}