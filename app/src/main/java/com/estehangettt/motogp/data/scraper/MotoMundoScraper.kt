package com.estehangettt.motogp.data.scraper

import com.estehangettt.motogp.data.EventResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.net.URLEncoder

class MotoMundoScraper {
    
    suspend fun findEventUrl(event: EventResponse): String? = withContext(Dispatchers.IO) {
        val country = event.circuit?.country?.lowercase()?.trim() ?: ""
        val circuitName = event.circuit?.name?.lowercase()?.trim() ?: ""
        
        val keywords = mutableListOf<String>()
        
        // Ambil keyword dari nama sirkuit (misal: "goiania", "americas", "jerez")
        circuitName.split(" ").forEach { word ->
            if (word.length > 4 && word != "circuit" && word != "international" && word != "autodromo" && word != "grand" && word != "prix") {
                keywords.add(word)
            }
        }
        
        // Ambil keyword dari negara
        when {
            country.contains("united states") -> keywords.add("americas")
            country.contains("france") -> keywords.add("french")
            country.contains("spain") -> keywords.add("spanish")
            country.contains("great britain") -> keywords.add("british")
            country.contains("italy") -> keywords.add("italian")
            country.contains("czech") -> keywords.add("brno")
            country.contains("san marino") -> keywords.add("misano")
            country.contains("netherlands") -> keywords.add("dutch")
            country.contains("brazil") -> keywords.add("brazil")
            else -> if (country.isNotBlank()) keywords.add(country)
        }

        for (keyword in keywords.distinct()) {
            if (keyword.isBlank()) continue
            
            try {
                val searchUrl = "https://motomundo.net/?s=motogp+2026+${URLEncoder.encode(keyword, "UTF-8")}"
                val doc = Jsoup.connect(searchUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .timeout(10000)
                    .get()

                // Cari link KHUSUS di dalam judul artikel (h2 a, h3 a)
                val links = doc.select("h2 a, h3 a, .entry-title a, .post-title a")
                for (link in links) {
                    val href = link.attr("href").lowercase()
                    
                    // Harus ada tahun dan watch-online
                    if (href.contains("motomundo.net") && 
                        href.contains("motogp-2026") && 
                        href.contains("watch-online")) {
                        return@withContext link.attr("href")
                    }
                }
            } catch (e: Exception) {
                // Lanjut
            }
        }
        null
    }

    suspend fun getEmbedUrl(motomundoUrl: String, sessionKeyword: String): String? = withContext(Dispatchers.IO) {
        try {
            val doc = Jsoup.connect(motomundoUrl)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .timeout(10000)
                .get()

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
