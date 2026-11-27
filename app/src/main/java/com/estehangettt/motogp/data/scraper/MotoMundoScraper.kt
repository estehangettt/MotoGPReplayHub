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
        
        // Kumpulan keyword yang PASTI dipakai MotoMundo
        val keywords = mutableListOf<String>()
        
        when {
            country.contains("united states") -> keywords.add("americas") // COTA selalu americas
            country.contains("france") -> keywords.add("french")
            country.contains("spain") -> keywords.add("spanish")
            country.contains("great britain") -> keywords.add("british")
            country.contains("italy") -> keywords.add("italian")
            country.contains("czech") -> keywords.add("brno")
            country.contains("san marino") -> keywords.add("misano")
            country.contains("netherlands") -> keywords.add("dutch")
            country.contains("portugal") -> keywords.add("portugal")
            country.contains("indonesia") -> keywords.add("mandalika")
            country.contains("thailand") -> keywords.add("thailand")
            country.contains("qatar") -> keywords.add("qatar")
            else -> if (country.isNotBlank()) keywords.add(country)
        }
        
        // Tambahin keyword dari nama sirkuit
        circuitName.split(" ").forEach { word ->
            if (word.length > 4 && word != "circuit" && word != "international" && word != "autodromo" && word != "grand" && word != "prix") {
                keywords.add(word)
            }
        }

        for (keyword in keywords.distinct()) {
            if (keyword.isBlank()) continue
            
            // Pakai endpoint SEARCH WP REST API
            val searchQuery = "motogp $year $keyword"
            val url = "https://motomundo.net/wp-json/wp/v2/search?search=${java.net.URLEncoder.encode(searchQuery, "UTF-8")}&per_page=10&_fields=url,title"
            
            try {
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()
                val body = response.body?.string() ?: continue
                val jsonArray = JSONArray(body)
                
                for (i in 0 until jsonArray.length()) {
                    val post = jsonArray.getJSONObject(i)
                    val link = post.getString("url").lowercase()
                    
                    // FILTER MUTLAK:
                    // 1. Harus ada 'motogp' (anti WCR/F1)
                    // 2. Harus ada 'watch-online'
                    // 3. Harus ada keyword (anti ketuker)
                    if (link.contains("motogp-$year") && 
                        link.contains("watch-online") && 
                        link.contains(keyword)) {
                        return@withContext post.getString("url")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        null
    }

    suspend fun getEmbedUrl(motomundoUrl: String, sessionKeyword: String): String? = withContext(Dispatchers.IO) {
        try {
            // Wajib pakai header User-Agent saat buka halaman MotoMundo
            val request = Request.Builder()
                .url(motomundoUrl)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .build()
            val response = client.newCall(request).execute()
            val html = response.body?.string() ?: return@withContext null
            
            val doc = org.jsoup.Jsoup.parse(html)
            val detailsList = doc.select("details")
            for (details in detailsList) {
                val summary = details.selectFirst("summary")?.text() ?: continue
                
                // Pengecekan sesi yang lebih pintar:
                // Kalau nyari "RACE", pastiin BUKAN "SPRINT RACE"
                val isMatch = if (sessionKeyword.uppercase() == "RACE") {
                    summary.contains("race", ignoreCase = true) && !summary.contains("sprint", ignoreCase = true)
                } else {
                    summary.contains(sessionKeyword, ignoreCase = true)
                }
                
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
