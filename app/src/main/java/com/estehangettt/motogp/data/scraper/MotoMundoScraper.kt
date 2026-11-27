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
        
        val keywords = mutableListOf<String>()
        
        // Mapping nama negara
        when {
            country.contains("united states") -> keywords.add("usa")
            country.contains("france") -> keywords.add("french")
            country.contains("spain") -> keywords.add("spanish")
            country.contains("great britain") -> keywords.add("british")
            country.contains("italy") -> keywords.add("italian")
            country.contains("czech") -> keywords.add("brno")
            country.contains("san marino") -> keywords.add("misano")
            country.contains("netherlands") -> keywords.add("dutch")
            country.contains("south africa") -> keywords.add("south-africa")
            else -> if (country.isNotBlank()) keywords.add(country)
        }
        
        // Tambahan keyword dari nama sirkuit
        if (circuitName.contains("americas")) keywords.add("americas")
        if (circuitName.contains("jerez")) keywords.add("jerez")
        if (circuitName.contains("lemans") || circuitName.contains("le mans")) keywords.add("le-mans")
        if (circuitName.contains("mugello")) keywords.add("mugello")
        if (circuitName.contains("mandalika")) keywords.add("mandalika")
        if (circuitName.contains("algarve") || circuitName.contains("portimao")) keywords.add("portugal")

        for (keyword in keywords.distinct()) {
            if (keyword.isBlank()) continue
            
            try {
                val searchUrl = "https://motomundo.net/?s=motogp+$year+${URLEncoder.encode(keyword, "UTF-8")}"
                val doc = Jsoup.connect(searchUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .timeout(10000)
                    .get()

                val links = doc.select("a")
                for (link in links) {
                    val href = link.attr("href").lowercase()
                    
                    if (href.contains("motomundo.net") && 
                        href.contains("motogp-$year") && 
                        href.contains("watch-online") &&
                        href.contains(keyword.replace(" ", "-"))) {
                        return@withContext link.attr("href")
                    }
                }
            } catch (e: Exception) {
                // Lanjut keyword selanjutnya
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
