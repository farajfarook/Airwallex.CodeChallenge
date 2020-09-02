package com.airwallex.codechallenge.cache

import com.airwallex.codechallenge.models.RateChange
import com.airwallex.codechallenge.models.RateHistory
import com.airwallex.codechallenge.models.RateHistoryEntry
import io.mockk.*
import io.mockk.impl.annotations.RelaxedMockK
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant

internal class RateCacheServiceTest {

    @Nested
    inner class HistoryCache {

        private lateinit var rateCache: RateCacheService

        @RelaxedMockK
        private lateinit var cache: CacheService

        private val currencyPair = "TEST"
        private val history = RateHistory(listOf(RateHistoryEntry(Instant.MIN, 0.0)))


        @BeforeEach
        fun setup() {
            MockKAnnotations.init(this)
            rateCache = RateCacheServiceImpl(cache)
        }

        @Test
        fun `set history - success`() {
            rateCache.setHistory(currencyPair, history)
            verify { cache.set("history_${currencyPair}", history) }
        }

        @Test
        fun `get history - success`() {
            every { cache.get<RateHistory>("history_${currencyPair}") } returns history
            val outHistory = rateCache.getHistory(currencyPair)
            assertThat(outHistory).isNotNull
            assertThat(outHistory).isEqualTo(history)
        }

        @Test
        fun `history cache with empty currency pair - throw error`() {
            assertThatExceptionOfType(InvalidCurrencyPairException::class.java)
                .isThrownBy { rateCache.getHistory("") }
            assertThatExceptionOfType(InvalidCurrencyPairException::class.java)
                .isThrownBy { rateCache.setHistory("", history) }
        }
    }

    @Nested
    inner class ChangeCache {

        private lateinit var rateCache: RateCacheService

        @RelaxedMockK
        private lateinit var cache: CacheService

        private val currencyPair = "TEST"
        private val change = RateChange()

        @BeforeEach
        fun setup() {
            MockKAnnotations.init(this)
            rateCache = RateCacheServiceImpl(cache)
        }

        @Test
        fun `set history - success`() {
            rateCache.setChange(currencyPair, change)
            verify { cache.set("change_${currencyPair}", change) }
        }

        @Test
        fun `get history - success`() {
            every { cache.get<RateChange>("change_${currencyPair}") } returns change
            val outChange = rateCache.getChange(currencyPair)
            assertThat(outChange).isNotNull
            assertThat(outChange).isEqualTo(change)
        }

        @Test
        fun `history cache with empty currency pair - throw error`() {
            assertThatExceptionOfType(InvalidCurrencyPairException::class.java)
                .isThrownBy { rateCache.getChange("") }
            assertThatExceptionOfType(InvalidCurrencyPairException::class.java)
                .isThrownBy { rateCache.setChange("", change) }
        }
    }
}

