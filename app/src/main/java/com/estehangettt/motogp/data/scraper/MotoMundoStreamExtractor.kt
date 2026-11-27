package com.estehangettt.motogp.data.scraper

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class MotoMundoStreamExtractor {
    private val client = OkHttpClient()

    suspend fun extractM3u8(embedUrl: String): String? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(embedUrl)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .addHeader("Referer", "https://motomundo.net/")
                .build()

            val response = client.newCall(request).execute()
            val html = response.body?.string() ?: return@withContext null

            // Regex Dean Edwards Packer
            val regex = Regex("""eval\(function\(p,a,c,k,e,d\)\{.*?\}\('(.*?)',(\d+),(\d+),'(.*?)'\.split\('\|'\)\)\)""", RegexOption.DOT_MATCHES_ALL)
            val match = regex.find(html) ?: return@withContext null

            val pStr = match.groupValues[1]
            val c = match.groupValues[3].toIntOrNull() ?: return@withContext null
            val kList = match.groupValues[4].split("|")

            var unpacked = pStr
            // Logika Unpack (Base36)
            for (i in c - 1 downTo 0) {
                if (i < kList.size && kList[i].isNotEmpty()) {
                    val key = i.toString(36)
                    val word = kList[i]
                    // Ganti kata utuh aja
                    val regexKey = Regex("\\b$key\\b")
                    unpacked = regexKey.replace(unpacked, word)
                }
            }

            // Cari URL m3u8 di dalam string yang udah di-unpack
            val urlRegex = Regex("""(https?://[^\s"'\\]+)""")
            val urls = urlRegex.findAll(unpacked)
            for (urlMatch in urls) {
                val url = urlMatch.value
                if (url.contains(".m3u8")) {
                    return@withContext url
                }
            }
            return@withContext null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
