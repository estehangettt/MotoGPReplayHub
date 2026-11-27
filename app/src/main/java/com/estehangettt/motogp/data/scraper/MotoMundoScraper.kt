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
        
        // Kata kunci utama berdasarkan negara
        var mainKeyword = country
        
        // Mapping nama negara ke nama yang dipakai MotoMundo
        when {
            country.contains("united states") -> mainKeyword = "usa"
            country.contains("france") -> mainKeyword = "french"
            country.contains("spain") -> mainKeyword = "spanish"
            country.contains("great britain") -> mainKeyword = "british"
            country.contains("italy") -> mainKeyword = "italian"
            country.contains("san marino") -> mainKeyword = "misano"
            country.contains("czech") -> mainKeyword = "brno"
        }

        // Daftar keyword yang bakal dicoba
        val keywords = mutableListOf(mainKeyword)
        
        // Tambahin keyword dari nama sirkuit (misal: "jerez", "americas", "lemans")
        val words = circuitName.split(" ").filter { it.length > 3 && it != "circuit" && it != "international" && it != "grand" }
        keywords.addAll(words)

        for (keyword in keywords) {
            try {
                val searchUrl = "https://motomundo.net/?s=motogp+$year+${URLEncoder.encode(keyword, "UTF-8")}"
                val doc = Jsoup.connect(searchUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .timeout(10000)
                    .get()

                val links = doc.select("a[href]")
                for (link in links) {
                    val href = link.attr("href").lowercase()
                    
                    // SYARAT KETAT: Harus ada tahun, harus watch-online, DAN harus ada keyword-nya!
                    if (href.contains("motogp-$year") && 
                        href.contains("watch-online") && 
                        href.contains(keyword)) {
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
                
                // Pastikan nyari kata utuh (biar "RACE" gak nangkep "SPRINT" atau sebaliknya)
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
