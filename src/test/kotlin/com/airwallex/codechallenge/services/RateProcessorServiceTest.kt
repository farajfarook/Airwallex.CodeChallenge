package com.airwallex.codechallenge.services

import com.airwallex.codechallenge.models.ConversionRate
import com.airwallex.codechallenge.rules.AlertEvent
import com.airwallex.codechallenge.rules.AlertRule
import com.airwallex.codechallenge.rules.AlertType
import com.airwallex.codechallenge.rules.change.ChangeAlertEvent
import com.airwallex.codechallenge.rules.change.ChangeAlertRule
import com.airwallex.codechallenge.rules.spot.SpotAlertEvent
import com.airwallex.codechallenge.rules.spot.SpotAlertRule
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant

internal class RateProcessorServiceTest {

    @RelaxedMockK
    private lateinit var rule1: AlertRule
    @RelaxedMockK
    private lateinit var rule2: AlertRule
    @RelaxedMockK
    private lateinit var rule3: AlertRule

    private lateinit var processor: RateProcessorService

    @BeforeEach
    fun setup(){
        MockKAnnotations.init(this)
        processor = RateProcessorServiceImpl(listOf(rule1, rule2, rule3))
    }

    @Nested
    inner class ProcessTests {

        @Test
        fun `given a list of rules - all rules were called`() {

            val rate = ConversionRate(Instant.EPOCH, "AUDUSD", 1.1)
            processor.process(rate)
            verify { rule1.perform(rate) }
            verify { rule2.perform(rate) }
            verify { rule3.perform(rate) }
        }

        @Test
        fun `given a list of rules with alerts - alerts output success`() {

            val rate = ConversionRate(Instant.EPOCH, "AUDUSD", 1.1)

            every { rule1.perform(rate) } returns null
            val alert2 = ChangeAlertEvent(Instant.EPOCH, rate.currencyPair, AlertType.Falling, 102)
            every { rule2.perform(rate) } returns alert2
            val alert3 = SpotAlertEvent(Instant.EPOCH, rate.currencyPair)
            every { rule3.perform(rate) } returns alert3

            val alerts = processor.process(rate)
            Assertions.assertThat(alerts).hasSize(2)
            Assertions.assertThat(alerts).contains(alert2)
            Assertions.assertThat(alerts).contains(alert3)
        }

    }
}