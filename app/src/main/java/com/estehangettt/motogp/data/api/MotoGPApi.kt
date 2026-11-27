package com.estehangettt.motogp.data.api

import com.estehangettt.motogp.data.EventResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class MotoGPApi {
    private val client = OkHttpClient()
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    suspend fun getEvents(year: Int): List<EventResponse> = withContext(Dispatchers.IO) {
        val url = "https://api.motogp.pulselive.com/motogp/v1/events?seasonYear=$year"
        val request = Request.Builder().url(url).build()
        
        try {
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext emptyList()
            
            val type = Types.newParameterizedType(List::class.java, EventResponse::class.java)
            val adapter = moshi.adapter<List<EventResponse>>(type)
            val events = adapter.fromJson(body) ?: emptyList()
            
            // Filter cuma GP aja dan urutin berdasarkan tanggal (opsional)
            events.filter { it.kind == "GP" }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
