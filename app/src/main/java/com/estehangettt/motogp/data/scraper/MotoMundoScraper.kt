package com.estehangettt.motogp.data.scraper

import com.estehangettt.motogp.data.EventResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.net.URLDecoder
import java.net.URLEncoder

class MotoMundoScraper {
    
    suspend fun findEventUrl(event: EventResponse, year: Int): String? = withContext(Dispatchers.IO) {
        val country = event.circuit?.country?.lowercase()?.trim() ?: ""
        val circuitName = event.circuit?.name?.lowercase()?.trim() ?: ""
        val eventName = event.name?.lowercase()?.trim() ?: ""
        
        val keywords = mutableListOf<String>()
        
        // Translasi nama negara ke bahasa Inggris yg dipakai MotoMundo
        when {
            country.contains("united states") -> keywords.add("americas")
            country.contains("france") -> keywords.add("french")
            country.contains("spain") -> keywords.add("spanish")
            country.contains("great britain") -> keywords.add("british")
            country.contains("italy") -> keywords.add("italian")
            country.contains("czech") -> keywords.add("brno")
            country.contains("san marino") -> keywords.add("misano")
            else -> if (country.isNotBlank()) keywords.add(country)
        }
        
        // Tambahin nama sirkuit
        if (circuitName.contains("americas")) keywords.add("americas")
        if (circuitName.contains("jerez")) keywords.add("jerez")
        if (circuitName.contains("lemans")) keywords.add("lemans")
        if (circuitName.contains("mugello")) keywords.add("mugello")

        for (keyword in keywords.distinct()) {
            if (keyword.isBlank()) continue
            
            // Pakai DuckDuckGo HTML biar gak kena block, cari link persisnya
            val query = "site:motomundo.net motogp $year $keyword"
            try {
                val searchUrl = "https://html.duckduckgo.com/html/?q=${URLEncoder.encode(query, "UTF-8")}"
                val doc = Jsoup.connect(searchUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .timeout(10000)
                    .get()
                
                val links = doc.select("a.result__url")
                for (link in links) {
                    var href = link.attr("href")
                    // DuckDuckGo suka nge-encode URL, kita harus decode
                    if (href.contains("uddg=")) {
                        href = URLDecoder.decode(href.substringAfter("uddg=").substringBefore("&"), "UTF-8")
                    }
                    
                    // Pastiin beneran link MotoMundo tahun ini dan watch-online
                    if (href.contains("motomundo.net") && 
                        href.contains("motogp-$year") && 
                        href.contains("watch-online")) {
                        return@withContext href
                    }
                }
            } catch (e: Exception) {
                // Lanjut keyword selanjutnya kalau error
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
                
                // Pastiin nyari kata utuh (biar "RACE" gak nangkep "SPRINT")
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
