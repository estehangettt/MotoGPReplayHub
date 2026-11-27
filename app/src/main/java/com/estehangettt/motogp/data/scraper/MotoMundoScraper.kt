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
        
        // Bikin daftar kata kunci pencarian (dari negara & sirkuit)
        val keywords = mutableListOf<String>()
        if (country.isNotEmpty()) keywords.add(country)
        
        // Mapping nama negara khusus
        when {
            country.contains("united states") -> keywords.add("usa")
            country.contains("france") -> keywords.add("french")
            country.contains("spain") -> keywords.add("spanish")
            country.contains("great britain") -> keywords.add("british")
            country.contains("italy") -> keywords.add("italian")
        }
        
        // Ambil potongan nama sirkuit (misal: "americas" dari "Circuit of the Americas")
        val words = circuitName.split(" ").filter { it.length > 3 && it != "circuit" && it != "international" && it != "grand" }
        keywords.addAll(words)

        for (keyword in keywords) {
            val searchUrl = "https://motomundo.net/?s=motogp+$year+${URLEncoder.encode(keyword, "UTF-8")}"
            try {
                val doc = Jsoup.connect(searchUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .timeout(10000)
                    .get()

                val links = doc.select("a[href]")
                for (link in links) {
                    val href = link.attr("href")
                    if (href.contains("motogp-$year", ignoreCase = true) && 
                        href.contains("watch-online", ignoreCase = true)) {
                        return@withContext href
                    }
                }
            } catch (e: Exception) {
                // Lanjut ke keyword selanjutnya kalau error
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
                if (summary.contains(sessionKeyword, ignoreCase = true)) {
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
