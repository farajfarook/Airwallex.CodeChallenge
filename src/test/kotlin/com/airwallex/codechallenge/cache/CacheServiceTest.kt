package com.airwallex.codechallenge.cache

import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class CacheServiceTest {

    @Nested
    inner class CacheSetAndGet {

        private lateinit var cache: CacheService

        inner class CacheObj (var id:String)

        @BeforeEach
        fun setup() {
            cache = CacheServiceImpl()
        }

        @Test
        fun `cache with valid key`() {

            var value = CacheObj("123")
            cache.set("test", value)

            var outValue = cache.get<CacheObj>("test")

            assertThat(outValue).isNotNull
            assertThat(outValue).isEqualTo(value)
        }

        @Test
        fun `cache with invalid key - throw error`() {

            val value = CacheObj("123")
            assertThatExceptionOfType(InvalidCacheKeyException::class.java)
                .isThrownBy { cache.set("", value) }
        }
    }
}