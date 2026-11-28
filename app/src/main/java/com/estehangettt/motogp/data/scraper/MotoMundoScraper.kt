package com.estehangettt.motogp.data.scraper

import com.estehangettt.motogp.data.EventResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray

class MotoMundoScraper {
    private val client = OkHttpClient()

    // 1. CARI URL HALAMAN BALAPAN (Berdasarkan Nomor Ronde)
    suspend fun findEventUrl(event: EventResponse, year: Int, round: Int): String? = withContext(Dispatchers.IO) {
        // Kita tembak API WordPress MotoMundo, cari yang judulnya persis "MotoGP 2026 Round XX"
        val searchQuery = "MotoGP 2026 Round $round"
        val url = "https://motomundo.net/wp-json/wp/v2/search?search=${java.net.URLEncoder.encode(searchQuery, "UTF-8")}&per_page=5&_fields=url,title"
        
        try {
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext null
            val jsonArray = JSONArray(body)
            
            for (i in 0 until jsonArray.length()) {
                val post = jsonArray.getJSONObject(i)
                val link = post.getString("url").lowercase()
                val title = post.getString("title").lowercase()
                
                // FILTER MUTLAK:
                // Harus ada 'motogp-2026' (anti WCR), harus ada 'round-X' atau judulnya ada round
                if (link.contains("motogp-2026") && 
                    link.contains("watch-online") &&
                    (link.contains("round-$round") || title.contains("round $round"))) {
                    return@withContext post.getString("url")
                }
            }
            
            // Kalau ronde gak nemu (misal API MotoGP urutannya beda sama MotoMundo),
            // Kita Fallback ke nama negara
            return@withContext findFallbackUrl(event, year)
            
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun findFallbackUrl(event: EventResponse, year: Int): String? {
        val country = event.circuit?.country?.lowercase() ?: return null
        val searchQuery = "MotoGP 2026 $country"
        val url = "https://motomundo.net/wp-json/wp/v2/search?search=${java.net.URLEncoder.encode(searchQuery, "UTF-8")}&per_page=5&_fields=url"
        
        try {
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return null
            val jsonArray = JSONArray(body)
            
            for (i in 0 until jsonArray.length()) {
                val post = jsonArray.getJSONObject(i)
                val link = post.getString("url").lowercase()
                // Harus MotoGP, jangan WCR
                if (link.contains("motogp-$year") && link.contains("watch-online")) {
                    return post.getString("url")
                }
            }
        } catch (e: Exception) {}
        return null
    }

    // 2. CARI LINK VIDEO DI DALAM HALAMAN (Khusus MotoGP)
    suspend fun getEmbedUrl(motomundoUrl: String, sessionKeyword: String): String? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(motomundoUrl)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .build()
            val response = client.newCall(request).execute()
            val html = response.body?.string() ?: return@withContext null
            
            val doc = org.jsoup.Jsoup.parse(html)
            
            // Cari heading yang tulisannya "MotoGP" (Biar gak salah ambil Moto2/Moto3)
            val motogpHeading = doc.select("h2:contains(MotoGP), h3:contains(MotoGP)").firstOrNull()
            
            // Kumpulin semua tag <details> yang ada di bawah heading MotoGP
            val validDetails = mutableListOf<org.jsoup.nodes.Element>()
            
            if (motogpHeading != null) {
                var sibling = motogpHeading.nextElementSibling()
                while (sibling != null) {
                    val text = sibling.text()
                    // Kalau udah masuk seksi Moto2, berhenti nyari
                    if (text.contains("Moto2", ignoreCase = true) || text.contains("Moto3", ignoreCase = true)) {
                        break
                    }
                    // Kumpulin <details> di seksi MotoGP
                    validDetails.addAll(sibling.select("details"))
                    if (sibling.tagName() == "details") validDetails.add(sibling)
                    
                    sibling = sibling.nextElementSibling()
                }
            } else {
                // Fallback kalau heading gak ketemu, ambil semua details
                validDetails.addAll(doc.select("details"))
            }

            // Cari sesi (RACE/FP1/Q2) di dalam list details MotoGP
            for (details in validDetails) {
                val summary = details.selectFirst("summary")?.text() ?: continue
                
                // Regex anti SPRINT ketuker sama RACE
                val isMatch = if (sessionKeyword.uppercase() == "RACE") {
                    summary.contains("race", true) && !summary.contains("sprint", true)
                } else {
                    summary.contains(sessionKeyword, true)
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
