package com.estehangettt.motogp.data.scraper

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

class MotoMundoScraper {
    
    suspend fun findEventUrl(round: Int, year: Int): String? = withContext(Dispatchers.IO) {
        // Cari berdasarkan nomor ronde! (contoh: motogp 2026 round 3)
        val searchUrl = "https://motomundo.net/?s=motogp+$year+round+$round"
        
        try {
            val doc = Jsoup.connect(searchUrl)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .timeout(10000)
                .get()

            val links = doc.select("a[href]")
            for (link in links) {
                val href = link.attr("href").lowercase()
                
                // Harus ada "motogp-2026", "watch-online", DAN "round-X"
                if (href.contains("motomundo.net") && 
                    href.contains("motogp-$year") && 
                    href.contains("watch-online")) {
                    
                    // Cek apakah ini ronde yang bener (antisipasi round-1 ketuker sama round-10)
                    val regexRound = Regex("round-$round-")
                    val regexRoundZero = Regex("round-0$round-") // Kadang ada yang pake 01
                    
                    if (regexRound.containsMatchIn(href) || regexRoundZero.containsMatchIn(href)) {
                        return@withContext link.attr("href")
                    }
                }
            }
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
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
