package com.udaan.child.data.model

data class Instrument(
    val securityId: String,
    val tradingSymbol: String,
    val displayName: String,
    val exchangeSegment: String,
    val instrument: String
)
