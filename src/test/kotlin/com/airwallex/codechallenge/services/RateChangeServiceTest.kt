package com.airwallex.codechallenge.services

import com.airwallex.codechallenge.cache.RateCacheService
import com.airwallex.codechallenge.models.ConversionRate
import com.airwallex.codechallenge.models.RateChange
import com.airwallex.codechallenge.models.RateChangeType
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource
import java.time.Duration
import java.time.Instant

internal class RateChangeServiceTest {

    @RelaxedMockK
    private lateinit var cache: RateCacheService

    private lateinit var service: RateChangeService

    @BeforeEach
    fun setup(){
        MockKAnnotations.init(this)
        service = RateChangeServiceImpl(cache)
    }

    @Nested
    inner class GetChangeType {

        private val currencyPair = "TEST"

        @Test
        fun `verify raise in rate - Success Raise`() {
            every { cache.getChange(currencyPair) } returns RateChange(rate = 1.2)
            val rate = ConversionRate(Instant.EPOCH, currencyPair, 1.3)
            Assertions.assertThat(service.getChangeType(rate)).isEqualTo(RateChangeType.Raise)
        }

        @Test
        fun `verify fall in rate - Success Fall`() {
            every { cache.getChange(currencyPair) } returns RateChange(rate = 1.2)
            val rate = ConversionRate(Instant.EPOCH, currencyPair, 1.3)
            Assertions.assertThat(service.getChangeType(rate)).isEqualTo(RateChangeType.Raise)
        }

        @Test
        fun `same previous rate in cache - Success None`() {
            every { cache.getChange(currencyPair) } returns RateChange(rate = 1.2)
            val rate = ConversionRate(Instant.EPOCH, currencyPair, 1.2)
            Assertions.assertThat(service.getChangeType(rate)).isEqualTo(RateChangeType.None)
        }

        @Test
        fun `negative rate in cache - Success fall`() {
            every { cache.getChange(currencyPair) } returns RateChange(rate = 1.2)
            val rate = ConversionRate(Instant.EPOCH, currencyPair, -1.2)
            Assertions.assertThat(service.getChangeType(rate)).isEqualTo(RateChangeType.Fall)
        }
    }

    @Nested
    inner class GetCurrentChangeType {
        @Test
        fun `success`() {
            val currencyPair = "TEST"
            every { cache.getChange(currencyPair) } returns RateChange(RateChangeType.Fall)
            Assertions.assertThat(service.getCurrentChangeType(currencyPair)).isEqualTo(RateChangeType.Fall)
        }
    }

    @Nested
    inner class GetCurrentChangeTime {
        @Test
        fun `success`() {
            val currencyPair = "TEST"
            val currentTime = Instant.EPOCH
            every { cache.getChange(currencyPair) } returns RateChange(timestamp = currentTime)
            Assertions.assertThat(service.getCurrentChangeTime(currencyPair)).isEqualTo(currentTime)
        }
    }

    @Nested
    inner class ResetTo {
        @Test
        fun `success`() {
            val rate = ConversionRate(Instant.EPOCH, "TEST", 1.3)
            service.resetTo(rate, RateChangeType.Fall)
            verify { cache.setChange(rate.currencyPair, match {
                t -> t.rate == rate.rate
                    && t.alertTimestamp == Instant.MIN
                    && t.timestamp == rate.timestamp
                    && t.type == RateChangeType.Fall
            }) }
        }
    }

    @Nested
    inner class UpdateAlertTimer {
        @Test
        fun `success`() {
            val alertTime = Instant.EPOCH
            val rateTime = Instant.EPOCH.plusSeconds(100)
            val currencyPair = "TEST"

            every { cache.getChange(currencyPair) } returns RateChange(RateChangeType.Raise, 1.1, rateTime)

            service.updateAlertTimer(currencyPair, alertTime)

            verify { cache.setChange(currencyPair, match {
                    t -> t.rate == 1.1
                    && t.alertTimestamp == alertTime
                    && t.timestamp == rateTime
                    && t.type == RateChangeType.Raise
            }) }
        }
    }

    @Nested
    inner class IsTimeToAlert {

        private val currencyPair = "TEST"

        @ParameterizedTest
        @CsvSource(
            "10, 0, false",
            "15, 0, false",
            "13, 120, false",
            "15, 60, true",
            "15, 120, true",
            "15, 30, false",
            "17, 60, true",
            "18, 120, true",
            "16, 30, false"
        )
        fun `changed x mins before, last alert y secs before - alert`(x: Long, y: Long, alert: Boolean) {

            val timestamp = Instant.now().minusSeconds(Duration.ofMinutes(x).seconds)
            val alertTimestamp = Instant.now().minusSeconds(y)

            every { cache.getChange(currencyPair) } returns RateChange(timestamp = timestamp, alertTimestamp = alertTimestamp)
            assert(service.isTimeToAlert(currencyPair, Instant.now(), Duration.ofSeconds(60), Duration.ofMinutes(15)) == alert)
        }
    }
}