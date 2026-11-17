package com.example.util

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs

sealed interface PhoneValidationResult {
    data class Valid(val normalizedPhone: String?) : PhoneValidationResult
    data object InvalidFormat : PhoneValidationResult
}

object KhataFormatters {

    private val pakistanPhoneRegex = Regex("^03\\d{9}$")

    fun validatePakistanPhone(rawInput: String): PhoneValidationResult {
        val cleaned = rawInput.replace(Regex("[\\s\\-()]"), "").trim()
        if (cleaned.isEmpty()) {
            return PhoneValidationResult.Valid(null)
        }
        return if (pakistanPhoneRegex.matches(cleaned)) {
            PhoneValidationResult.Valid(cleaned)
        } else {
            PhoneValidationResult.InvalidFormat
        }
    }

    fun formatPhoneForDisplay(phone: String?): String? {
        if (phone.isNullOrBlank()) return null
        return if (phone.length == 11 && phone.startsWith("03")) {
            "${phone.substring(0, 4)}-${phone.substring(4)}"
        } else {
            phone
        }
    }

    fun parseAmountToPaisa(rawInput: String): Long? {
        val cleaned = rawInput.replace(",", "").trim()
        if (cleaned.isEmpty()) return null
        val decimal = cleaned.toBigDecimalOrNull() ?: return null
        if (decimal <= BigDecimal.ZERO) return null
        // Cap at 999,999,999 Rs to avoid overflow
        if (decimal > BigDecimal("999999999")) return null
        val paisa = decimal.multiply(BigDecimal(100))
            .setScale(0, RoundingMode.HALF_UP)
            .longValueExact()
        return if (paisa > 0L) paisa else null
    }

    fun paisaToInputString(paisa: Long): String {
        val absPaisa = abs(paisa)
        val whole = absPaisa / 100
        val fraction = absPaisa % 100
        return if (fraction == 0L) {
            whole.toString()
        } else {
            String.format(Locale.US, "%d.%02d", whole, fraction).trimEnd('0').trimEnd('.')
        }
    }

    fun formatRs(paisa: Long, withPrefix: Boolean = true): String {
        val absPaisa = abs(paisa)
        val whole = absPaisa / 100
        val fraction = absPaisa % 100
        val symbols = DecimalFormatSymbols(Locale.US)
        val formattedNumber = if (fraction == 0L) {
            DecimalFormat("#,##0", symbols).format(whole)
        } else {
            val amount = absPaisa.toDouble() / 100.0
            DecimalFormat("#,##0.##", symbols).format(amount)
        }
        return if (withPrefix) "Rs $formattedNumber" else formattedNumber
    }

    fun todayUtcMidnightMillis(): Long {
        val local = Calendar.getInstance()
        val year = local.get(Calendar.YEAR)
        val month = local.get(Calendar.MONTH)
        val day = local.get(Calendar.DAY_OF_MONTH)
        return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(year, month, day, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    fun formatUtcDate(millis: Long, locale: Locale = Locale.getDefault()): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", locale).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        return sdf.format(Date(millis))
    }

    fun initialsForName(name: String): String {
        val parts = name.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
        return when {
            parts.isEmpty() -> "?"
            parts.size == 1 -> parts[0].take(2).uppercase(Locale.US)
            else -> "${parts[0].first()}${parts[1].first()}".uppercase(Locale.US)
        }
    }
}
