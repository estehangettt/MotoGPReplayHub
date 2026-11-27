package com.estehangettt.motogp.data.scraper

import com.estehangettt.motogp.data.EventResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.net.URLEncoder

class MotoMundoScraper {
    
    suspend fun findEventUrl(event: EventResponse, year: Int): String? = withContext(Dispatchers.IO) {
        val country = event.circuit?.country?.lowercase()?.trim() ?: ""
        val circuitName = event.circuit?.name?.lowercase()?.trim() ?: ""
        
        // Kumpulan keyword alternatif
        val keywords = mutableListOf<String>()
        
        // Mapping nama negara
        when {
            country.contains("united states") -> { keywords.add("usa"); keywords.add("americas") }
            country.contains("france") -> keywords.add("french")
            country.contains("spain") -> keywords.add("spanish")
            country.contains("great britain") -> keywords.add("british")
            country.contains("italy") -> keywords.add("italian")
            country.contains("czech") -> keywords.add("brno")
            country.contains("san marino") -> keywords.add("misano")
            country.contains("netherlands") -> keywords.add("dutch")
            else -> if (country.isNotBlank()) keywords.add(country)
        }
        
        // Ambil kata dari nama sirkuit
        circuitName.split(" ").forEach { word ->
            if (word.length > 4 && word != "circuit" && word != "international" && word != "autodromo" && word != "grand" && word != "prix") {
                keywords.add(word)
            }
        }

        for (keyword in keywords.distinct()) {
            if (keyword.isBlank()) continue
            val cleanKeyword = keyword.replace(" ", "-")
            
            try {
                // Cari via search internal MotoMundo
                val searchUrl = "https://motomundo.net/?s=motogp+$year+${URLEncoder.encode(cleanKeyword, "UTF-8")}"
                val doc = Jsoup.connect(searchUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .timeout(10000)
                    .get()

                // Ambil SEMUA link di halaman itu
                val links = doc.select("a")
                for (link in links) {
                    val href = link.attr("href").lowercase()
                    
                    // FILTER MUTLAK: URL harus punya kata kunci ini
                    if (href.contains("motomundo.net") && 
                        href.contains("motogp-$year") && 
                        href.contains("watch-online") &&
                        href.contains(cleanKeyword)) { // <--- INI KUNCI ANTI-KETUKER
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
                
                // Cek sesi yang akurat (RACE gak bakal nangkep SPRINT)
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
