package com.airwallex.codechallenge.rules

import com.airwallex.codechallenge.models.ConversionRate
import com.airwallex.codechallenge.models.RateChangeType
import com.airwallex.codechallenge.rules.change.ChangeAlertEvent
import com.airwallex.codechallenge.rules.change.ChangeAlertRule
import com.airwallex.codechallenge.rules.spot.SpotAlertEvent
import com.airwallex.codechallenge.rules.spot.SpotAlertRule
import com.airwallex.codechallenge.services.RateChangeService
import com.airwallex.codechallenge.services.RateHistoryService
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant

internal class SpotAlertRuleTest {

    private val currencyPair = "TEST"

    @RelaxedMockK
    private lateinit var service: RateHistoryService

    private lateinit var rule: SpotAlertRule

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        rule = SpotAlertRule(service)
    }

    @Nested
    inner class Perform {

        @Test
        fun `When above average - alert`() {
            every { service.getRates(currencyPair) } returns listOf(1.0,1.0,1.0,1.0)

            val rate = ConversionRate(Instant.now(), currencyPair, 1.1)

            val alert = rule.perform(rate) as SpotAlertEvent

            verify { service.recycle(currencyPair, Duration.ofMinutes(5)) }
            verify { service.getRates(currencyPair) }
            verify { service.update(rate) }

            assertThat(alert).isNotNull
            assertThat(alert.alert).isEqualTo(AlertType.SpotChange)
            assertThat(alert.currencyPair).isEqualTo(currencyPair)
            assertThat(alert.timestamp).isEqualTo(rate.timestamp)
        }

        @Test
        fun `When below average - no alert`() {
            every { service.getRates(currencyPair) } returns listOf(1.0,1.0,1.0,1.0)

            val rate = ConversionRate(Instant.now(), currencyPair, 1.05)

            val alert = rule.perform(rate)

            verify { service.recycle(currencyPair, Duration.ofMinutes(5)) }
            verify { service.getRates(currencyPair) }
            verify { service.update(rate) }

            assertThat(alert).isNull()
        }
    }
}