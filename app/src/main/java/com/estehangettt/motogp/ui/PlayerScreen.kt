package com.estehangettt.motogp.ui

import android.annotation.SuppressLint
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.ui.PlayerView
import com.estehangettt.motogp.ui.theme.MotoGPBlack
import com.estehangettt.motogp.ui.theme.MotoGPRed
import kotlinx.coroutines.delay

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PlayerScreen(embedUrl: String) {
    var m3u8Url by remember { mutableStateOf<String?>(null) }
    
    // Setup ExoPlayer
    val context = androidx.compose.ui.platform.LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
        }
    }

    // Pantau kalau m3u8 udah ketemu
    LaunchedEffect(m3u8Url) {
        m3u8Url?.let { url ->
            val dataSourceFactory = DefaultHttpDataSource.Factory()
                .setDefaultRequestProperties(
                    mapOf(
                        "Referer" to "https://lplaygo.com/",
                        "Origin" to "https://lplaygo.com",
                        "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
                    )
                )
            val mediaSource = HlsMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(url))
            
            exoPlayer.setMediaSource(mediaSource)
            exoPlayer.prepare()
        }
    }

    // Matiin player kalau keluar layar
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MotoGPBlack)) {
        if (m3u8Url != null) {
            // Tampilin Video Kalau link udah dapet
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = true
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Tampilin Loading sambil nunggu WebView nangkep link
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(color = MotoGPRed)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Mencari Link Video...", color = Color.White)
            }
        }

        // HIDDEN WEBVIEW (Ukuran 0x0, gak keliatan)
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    webViewClient = object : WebViewClient() {
                        override fun shouldInterceptRequest(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): android.webkit.WebResourceResponse? {
                            val url = request?.url.toString()
                            // FILTER: Cari link m3u8, abaikan file .ts (segment)
                            if (url.contains(".m3u8") && !url.contains(".ts")) {
                                m3u8Url = url
                            }
                            return super.shouldInterceptRequest(view, request)
                        }
                    }
                    loadUrl(embedUrl)
                }
            },
            modifier = Modifier.size(0.dp) // Bikin gaib
        )
    }
}
