package com.estehangettt.motogp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.ui.PlayerView
import com.estehangettt.motogp.data.scraper.MotoMundoStreamExtractor
import com.estehangettt.motogp.ui.theme.MotoGPBlack
import com.estehangettt.motogp.ui.theme.MotoGPRed
import com.estehangettt.motogp.ui.theme.GlassWhite

@Composable
fun PlayerScreen(embedUrl: String, onBackClick: () -> Unit) {
    var m3u8Url by remember { mutableStateOf<String?>(null) }
    var debugStatus by remember { mutableStateOf("Mengekstrak Link Video (Native Kotlin)...") }
    
    val context = androidx.compose.ui.platform.LocalContext.current
    val exoPlayer = remember { ExoPlayer.Builder(context).build().apply { playWhenReady = true } }
    val extractor = remember { MotoMundoStreamExtractor() }

    // 1. Ekstrak m3u8 pakai Kotlin (Tanpa WebView)
    LaunchedEffect(embedUrl) {
        val url = extractor.extractM3u8(embedUrl)
        if (url != null) {
            m3u8Url = url
        } else {
            debugStatus = "Gagal mengekstrak link .m3u8. Format JS mungkin berubah."
        }
    }

    // 2. Putar Video kalau m3u8 dapet
    LaunchedEffect(m3u8Url) {
        m3u8Url?.let { url ->
            val dataSourceFactory = DefaultHttpDataSource.Factory()
                .setDefaultRequestProperties(
                    mapOf(
                        "Referer" to "https://lplaygo.com/",
                        "Origin" to "https://lplaygo.com",
                        "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64)"
                    )
                )
            val mediaSource = HlsMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(url))
            exoPlayer.setMediaSource(mediaSource)
            exoPlayer.prepare()
        }
    }

    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    Box(modifier = Modifier.fillMaxSize().background(MotoGPBlack)) {
        if (m3u8Url != null) {
            // Player
            AndroidView(
                factory = { ctx -> PlayerView(ctx).apply { player = exoPlayer; useController = true } },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Loading / Error Screen
            Column(
                modifier = Modifier.align(Alignment.Center).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(color = MotoGPRed)
                Spacer(modifier = Modifier.height(16.dp))
                Text(debugStatus, color = Color.White)
            }
        }

        // Tombol Back
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 40.dp, start = 16.dp)
                .background(GlassWhite, RoundedCornerShape(50))
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
        }
    }
}
