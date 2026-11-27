package com.estehangettt.motogp.data

import java.io.Serializable

data class EventResponse(
    val id: String,
    val name: String,
    val kind: String,
    val date_start: String?,
    val circuit: CircuitData?,
    val assets: List<Asset>?
) : Serializable

data class CircuitData(
    val id: String,
    val name: String,
    val country: String,
    val capacity: Int?,
    val track: TrackData?
) : Serializable

data class TrackData(
    val lenght_units: LengthUnits?,
    val assets: TrackAssets?
) : Serializable

data class LengthUnits(
    val kiloMeters: Double?
) : Serializable

data class TrackAssets(
    val simple: AssetImage?
) : Serializable

data class AssetImage(
    val path: String
) : Serializable

data class Asset(
    val type: String,
    val path: String
) : Serializable
