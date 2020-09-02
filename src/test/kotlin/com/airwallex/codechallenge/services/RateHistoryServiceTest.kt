package com.airwallex.codechallenge.services

import com.airwallex.codechallenge.cache.RateCacheService
import com.airwallex.codechallenge.models.RateChange
import com.airwallex.codechallenge.models.RateChangeType
import com.airwallex.codechallenge.models.RateHistory
import com.airwallex.codechallenge.models.RateHistoryEntry
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant


internal class RateHistoryServiceTest {

    @RelaxedMockK
    private lateinit var cache: RateCacheService

    private lateinit var service: RateHistoryService

    @BeforeEach
    fun setup(){
        MockKAnnotations.init(this)
        service = RateHistoryServiceImpl(cache)
    }

    @Nested
    inner class Update {

    }

    @Nested
    inner class Recycle {

        @Test
        fun `with older rates - success filtered`() {
            val currencyPair = "TEST"

            val validRates = listOf(
                RateHistoryEntry(Instant.now(), 1.1),
                RateHistoryEntry(Instant.now().plusSeconds(Duration.ofMinutes(2).seconds), 1.2),
                RateHistoryEntry(Instant.now().plusSeconds(Duration.ofMinutes(3).seconds), 1.4),
                RateHistoryEntry(Instant.now().plusSeconds(Duration.ofMinutes(5).seconds), 1.5)
            )

            val rates = validRates.toMutableList();
            rates.addAll(listOf(
                RateHistoryEntry(Instant.now().plusSeconds(Duration.ofMinutes(6).seconds), 1.6),
                RateHistoryEntry(Instant.now().plusSeconds(Duration.ofMinutes(10).seconds), 1.6)
            ))

            every { cache.getHistory(currencyPair) } returns RateHistory(rates)

            service.recycle(currencyPair, Duration.ofMinutes(5))

            verify { cache.setHistory(currencyPair, match { h -> h.rates.containsAll(validRates) }) }
        }

        @Test
        fun `with non older rates - success without filtered`() {
            val currencyPair = "TEST"

            val validRates = listOf(
                RateHistoryEntry(Instant.now(), 1.1),
                RateHistoryEntry(Instant.now().plusSeconds(Duration.ofMinutes(2).seconds), 1.2),
                RateHistoryEntry(Instant.now().plusSeconds(Duration.ofMinutes(3).seconds), 1.4),
                RateHistoryEntry(Instant.now().plusSeconds(Duration.ofMinutes(5).seconds), 1.5),
                RateHistoryEntry(Instant.now().plusSeconds(Duration.ofMinutes(1).seconds), 1.5)
            )

            every { cache.getHistory(currencyPair) } returns RateHistory(validRates)

            service.recycle(currencyPair, Duration.ofMinutes(5))

            verify { cache.setHistory(currencyPair, match { h -> h.rates.containsAll(validRates) }) }
        }
    }

    @Nested
    inner class GetRates {

        @Test
        fun `success`() {
            val currencyPair = "TEST"

            val rates = listOf(
                RateHistoryEntry(Instant.now(), 1.1),
                RateHistoryEntry(Instant.now().plusSeconds(1), 1.2),
                RateHistoryEntry(Instant.now().plusSeconds(2), 1.1)
            )

            every { cache.getHistory(currencyPair) } returns RateHistory(rates)

            val outRates = service.getRates(currencyPair)

            Assertions.assertThat(outRates).containsExactly(1.1, 1.2, 1.1)
        }
    }
}