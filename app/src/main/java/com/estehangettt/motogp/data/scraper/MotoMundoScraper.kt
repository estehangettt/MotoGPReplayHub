package com.estehangettt.motogp.data.scraper

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

class MotoMundoScraper {
    suspend fun getEmbedUrl(motomundoUrl: String, sessionKeyword: String): String? = withContext(Dispatchers.IO) {
        try {
            val doc = Jsoup.connect(motomundoUrl)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .timeout(10000)
                .get()

            // Cari semua <details> (sesuai kode Python lo)
            val detailsList = doc.select("details")
            for (details in detailsList) {
                val summary = details.selectFirst("summary")?.text() ?: continue
                
                // Kalau judulnya cocok sama sesi yang diklik (misal: RACE)
                if (summary.contains(sessionKeyword, ignoreCase = true)) {
                    val iframes = details.select("iframe")
                    for (iframe in iframes) {
                        val src = iframe.attr("data-src").ifEmpty { iframe.attr("src") }
                        // Prioritaskan lplaygo atau motomundo
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
