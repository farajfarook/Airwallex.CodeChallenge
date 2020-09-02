package com.airwallex.codechallenge.rules

import com.airwallex.codechallenge.models.ConversionRate
import com.airwallex.codechallenge.models.RateChangeType
import com.airwallex.codechallenge.rules.change.ChangeAlertEvent
import com.airwallex.codechallenge.rules.change.ChangeAlertRule
import com.airwallex.codechallenge.services.RateChangeService
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

internal class ChangeAlertRuleTest {

    private val currencyPair = "TEST"

    @RelaxedMockK
    private lateinit var service: RateChangeService

    private lateinit var rule: ChangeAlertRule

    private val alertTimeAfter: Duration = Duration.ofMinutes(1)
    private val detectChangeAfter: Duration = Duration.ofMinutes(15)

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        rule = ChangeAlertRule(service)
    }

    @Nested
    inner class Perform {

        @Test
        fun `When raised and it's time - alert`() {
            every { service.isTimeToAlert(currencyPair, any(), alertTimeAfter, detectChangeAfter) } returns true

            val rate = ConversionRate(Instant.now(), currencyPair, 1.1)

            every { service.getChangeType(rate) } returns RateChangeType.Raise
            every { service.getCurrentChangeType(currencyPair) } returns RateChangeType.Raise

            every { service.getCurrentChangeTime(currencyPair) } returns Instant.now().minusSeconds(50)

            val alert = rule.perform(rate) as ChangeAlertEvent

            verify(exactly = 0) { service.resetTo(any(), any()) }
            verify { service.updateAlertTimer(currencyPair, rate.timestamp) }

            assertThat(alert).isNotNull
            assertThat(alert.alert).isEqualTo(AlertType.Raising)
            assertThat(alert.currencyPair).isEqualTo(currencyPair)
            assertThat(alert.timestamp).isEqualTo(rate.timestamp)
            assertThat(alert.seconds).isEqualTo(50)
        }
    }

    @Test
    fun `When raised continuesly and it's time - alert multiple`() {
        every { service.isTimeToAlert(currencyPair, any(), alertTimeAfter, detectChangeAfter) } returns true

        val rate = ConversionRate(Instant.now(), currencyPair, 1.1)

        every { service.getChangeType(any()) } returns RateChangeType.Raise
        every { service.getCurrentChangeType(currencyPair) } returns RateChangeType.Raise

        every { service.getCurrentChangeTime(currencyPair) } returns Instant.now().minusSeconds(50)

        val alert = rule.perform(rate) as ChangeAlertEvent

        verify(exactly = 0) { service.resetTo(any(), any()) }
        verify { service.updateAlertTimer(currencyPair, rate.timestamp) }

        assertThat(alert).isNotNull
        assertThat(alert.alert).isEqualTo(AlertType.Raising)
        assertThat(alert.currencyPair).isEqualTo(currencyPair)
        assertThat(alert.timestamp).isEqualTo(rate.timestamp)
        assertThat(alert.seconds).isEqualTo(50)

        val rate2 = ConversionRate(Instant.now().plusSeconds(2), currencyPair, 1.1)
        every { service.getCurrentChangeTime(currencyPair) } returns Instant.now().minusSeconds(60)

        val alert2 = rule.perform(rate2) as ChangeAlertEvent

        verify { service.updateAlertTimer(currencyPair, rate2.timestamp) }

        assertThat(alert2).isNotNull
        assertThat(alert2.alert).isEqualTo(AlertType.Raising)
        assertThat(alert2.currencyPair).isEqualTo(currencyPair)
        assertThat(alert2.timestamp).isEqualTo(rate2.timestamp)
        assertThat(alert2.seconds).isEqualTo(62)

    }


    @Test
    fun `When changes from raise to fall - no alert`() {
        val rate = ConversionRate(Instant.now(), currencyPair, 1.1)

        every { service.getChangeType(rate) } returns RateChangeType.Fall
        every { service.getCurrentChangeType(currencyPair) } returns RateChangeType.Raise

        every { service.getCurrentChangeTime(currencyPair) } returns Instant.now().minusSeconds(50)

        val alert = rule.perform(rate)

        verify { service.resetTo(rate, RateChangeType.Fall) }
        verify(exactly = 0) { service.updateAlertTimer(any(), any()) }
        verify(exactly = 0) { service.isTimeToAlert(any(), any(), any(), any()) }

        assertThat(alert).isNull()
    }

    @Test
    fun `When changes from fall to raise - no alert`() {
        val rate = ConversionRate(Instant.now(), currencyPair, 1.1)

        every { service.getChangeType(rate) } returns RateChangeType.Raise
        every { service.getCurrentChangeType(currencyPair) } returns RateChangeType.Fall

        every { service.getCurrentChangeTime(currencyPair) } returns Instant.now().minusSeconds(50)

        val alert = rule.perform(rate)

        verify { service.resetTo(rate, RateChangeType.Raise) }
        verify(exactly = 0) { service.updateAlertTimer(any(), any()) }
        verify(exactly = 0) { service.isTimeToAlert(any(), any(), any(), any()) }

        assertThat(alert).isNull()
    }
}