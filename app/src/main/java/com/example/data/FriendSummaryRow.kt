package com.example.data

data class FriendSummaryRow(
    val id: Long,
    val name: String,
    val phone: String?,
    val createdAt: Long,
    val netBalancePaisa: Long,
    val entryCount: Int,
    val lastActivityMillis: Long?
)
