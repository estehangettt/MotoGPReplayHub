package com.estehangettt.motogp.data.scraper

import com.estehangettt.motogp.data.EventResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray

class MotoMundoScraper {
    private val client = OkHttpClient()

    suspend fun findEventUrl(event: EventResponse, year: Int): String? = withContext(Dispatchers.IO) {
        val country = event.circuit?.country?.lowercase()?.trim() ?: ""
        val circuitName = event.circuit?.name?.lowercase()?.trim() ?: ""
        
        // 1. Ambil SEMUA post dari API WordPress MotoMundo (kategori 212 = 2026)
        val url = "https://motomundo.net/wp-json/wp/v2/posts?categories=212&per_page=100&_fields=link,slug"
        val request = Request.Builder().url(url).build()
        
        try {
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext null
            val jsonArray = JSONArray(body)
            
            // 2. Iterasi semua URL, cari yang cocok
            for (i in 0 until jsonArray.length()) {
                val post = jsonArray.getJSONObject(i)
                val slug = post.getString("slug").lowercase()
                val link = post.getString("link")
                
                // Cek apakah slug mengandung nama negara atau sirkuit
                var isMatch = false
                
                // Cek nama sirkuit (misal: "jerez", "americas", "goiania")
                circuitName.split(" ").forEach { word ->
                    if (word.length > 4 && slug.contains(word)) {
                        isMatch = true
                    }
                }
                
                // Cek negara (dengan mapping khusus)
                if (!isMatch) {
                    when {
                        country.contains("united states") && slug.contains("usa") -> isMatch = true
                        country.contains("france") && slug.contains("french") -> isMatch = true
                        country.contains("spain") && slug.contains("spanish") -> isMatch = true
                        country.contains("great britain") && slug.contains("british") -> isMatch = true
                        country.contains("italy") && slug.contains("italian") -> isMatch = true
                        country.contains("czech") && slug.contains("brno") -> isMatch = true
                        country.contains("san marino") && slug.contains("misano") -> isMatch = true
                        country.contains("netherlands") && slug.contains("dutch") -> isMatch = true
                        country.isNotBlank() && slug.contains(country) -> isMatch = true
                    }
                }

                if (isMatch) {
                    return@withContext link
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        null
    }

    suspend fun getEmbedUrl(motomundoUrl: String, sessionKeyword: String): String? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(motomundoUrl).build()
            val response = client.newCall(request).execute()
            val html = response.body?.string() ?: return@withContext null
            
            val doc = org.jsoup.Jsoup.parse(html)
            val detailsList = doc.select("details")
            for (details in detailsList) {
                val summary = details.selectFirst("summary")?.text() ?: continue
                val isMatch = summary.contains(Regex("\\b$sessionKeyword\\b", RegexOption.IGNORE_CASE))
                
                if (isMatch) {
                    val iframes = details.select("iframe")
                    for (iframe in iframes) {
                        val src = iframe.attr("data-src").ifEmpty { iframe.attr("src") }
                        if (src.contains("lplaygo") || src.contains("motomundo.top")) {
                            return@withContext src
                        }
                    }
                }
            }
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
