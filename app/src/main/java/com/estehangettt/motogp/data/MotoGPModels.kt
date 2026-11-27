package com.estehangettt.motogp.data

import com.squareup.moshi.Json

data class EventResponse(
    val id: String,
    val name: String,
    val kind: String,
    val circuit: CircuitData?,
    val assets: List<Asset>?
)

data class CircuitData(
    val id: String,
    val name: String,
    val country: String,
    val track: TrackData?
)

data class TrackData(
    val assets: TrackAssets?
)

data class TrackAssets(
    val simple: AssetImage?
)

data class AssetImage(
    val path: String
)

data class Asset(
    val type: String,
    val path: String
)
