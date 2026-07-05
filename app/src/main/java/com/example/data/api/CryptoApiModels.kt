package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CoinPrice(
    @Json(name = "usd") val usd: Double,
    @Json(name = "usd_24h_change") val usd24hChange: Double?
)

@JsonClass(generateAdapter = true)
data class CoinGeckoResponse(
    @Json(name = "bitcoin") val bitcoin: CoinPrice?,
    @Json(name = "ethereum") val ethereum: CoinPrice?,
    @Json(name = "litecoin") val litecoin: CoinPrice?,
    @Json(name = "dogecoin") val dogecoin: CoinPrice?
)

data class LiveBlockchainState(
    val btcPriceUsd: Double = 62500.0,
    val btc24hChange: Double = 2.45,
    val ethPriceUsd: Double = 3350.0,
    val eth24hChange: Double = -1.15,
    val ltcPriceUsd: Double = 82.50,
    val ltc24hChange: Double = 0.85,
    val dogePriceUsd: Double = 0.132,
    val doge24hChange: Double = 5.23,
    
    // Live network variables
    val bitcoinBlockHeight: Long = 850124,
    val recommendedBtcFeeSatVb: Double = 22.5,
    val recommendedEthFeeGwei: Double = 18.0
)
