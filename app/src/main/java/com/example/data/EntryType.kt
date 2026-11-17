package com.example.data

enum class EntryType(val code: String) {
    UDHAAR_DIYA("UDHAAR_DIYA"),       // I lent money -> friend owes me more (+)
    UDHAAR_LIYA("UDHAAR_LIYA"),       // I borrowed money -> I owe friend more (-)
    WAPSI_RECEIVED("WAPSI_RECEIVED"), // Friend paid me back -> reduces what friend owes me (-)
    WAPSI_PAID("WAPSI_PAID");         // I paid friend back -> reduces what I owe friend (+)

    fun signedDelta(amountPaisa: Long): Long = when (this) {
        UDHAAR_DIYA, WAPSI_PAID -> amountPaisa
        UDHAAR_LIYA, WAPSI_RECEIVED -> -amountPaisa
    }

    val isWapsi: Boolean
        get() = this == WAPSI_RECEIVED || this == WAPSI_PAID

    companion object {
        fun fromCode(code: String): EntryType {
            return entries.firstOrNull { it.code == code } ?: UDHAAR_DIYA
        }
    }
}
