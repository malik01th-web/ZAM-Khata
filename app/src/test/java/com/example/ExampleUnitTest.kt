package com.example

import com.example.util.KhataFormatters
import com.example.util.PhoneValidationResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun `pakistan phone validation accepts empty and 03XXXXXXXXX format`() {
        val emptyResult = KhataFormatters.validatePakistanPhone("")
        assertTrue(emptyResult is PhoneValidationResult.Valid)
        assertNull((emptyResult as PhoneValidationResult.Valid).normalizedPhone)

        val validResult = KhataFormatters.validatePakistanPhone("0300-1234567")
        assertTrue(validResult is PhoneValidationResult.Valid)
        assertEquals("03001234567", (validResult as PhoneValidationResult.Valid).normalizedPhone)

        val invalidShort = KhataFormatters.validatePakistanPhone("030012345")
        assertTrue(invalidShort is PhoneValidationResult.InvalidFormat)

        val invalidPrefix = KhataFormatters.validatePakistanPhone("04212345678")
        assertTrue(invalidPrefix is PhoneValidationResult.InvalidFormat)
    }

    @Test
    fun `amount parsing and Rs formatting handle whole and decimal amounts`() {
        assertEquals(150_000L, KhataFormatters.parseAmountToPaisa("1500"))
        assertEquals(150_050L, KhataFormatters.parseAmountToPaisa("1,500.5"))
        assertNull(KhataFormatters.parseAmountToPaisa("0"))
        assertNull(KhataFormatters.parseAmountToPaisa("-50"))
        assertNull(KhataFormatters.parseAmountToPaisa("abc"))

        assertEquals("Rs 1,500", KhataFormatters.formatRs(150_000L))
        assertEquals("Rs 1,500.5", KhataFormatters.formatRs(150_050L))
    }
}
