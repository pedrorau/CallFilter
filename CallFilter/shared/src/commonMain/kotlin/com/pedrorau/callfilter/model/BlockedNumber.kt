package com.pedrorau.callfilter.model

import kotlinx.serialization.Serializable

@Serializable
data class BlockedNumber(
    val id: String,
    val number: String,
    val addedTimestamp: Long
)
