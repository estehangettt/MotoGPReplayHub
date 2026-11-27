package com.estehangettt.motogp.ui

import android.annotation.SuppressLint
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.ui.PlayerView
import com.estehangettt.motogp.ui.theme.MotoGPBlack
import com.estehangettt.motogp.ui.theme.MotoGPRed
import com.estehangettt.motogp.ui.theme.GlassWhite

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PlayerScreen(embedUrl: String, onBackClick: () -> Unit) {
    var m3u8Url by remember { mutableStateOf<String?>(null) }
    
    val context = androidx.compose.ui.platform.LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
        }
    }

    LaunchedEffect(m3u8Url) {
        m3u8Url?.let { url ->
            val dataSourceFactory = DefaultHttpDataSource.Factory()
                .setDefaultRequestProperties(
                    mapOf(
                        "Referer" to "https://lplaygo.com/",
                        "Origin" to "https://lplaygo.com",
                        "User-Agent" to "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Mobile Safari/537.36"
                    )
                )
            val mediaSource = HlsMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(url))
            
            exoPlayer.setMediaSource(mediaSource)
            exoPlayer.prepare()
        }
    }

    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    Box(modifier = Modifier.fillMaxSize().background(MotoGPBlack)) {
        
        // TAMPILIN WEBVIEW SECARA VISUAL BUAT DEBUGGING
        Column(modifier = Modifier.fillMaxSize()) {
            if (m3u8Url == null) {
                Text(
                    text = "DEBUG MODE: Menunggu m3u8...",
                    color = Color.Red,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 40.dp, start = 16.dp)
                )
                Text(
                    text = "URL: $embedUrl",
                    color = Color.Gray,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        setBackgroundColor(android.graphics.Color.BLACK)
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.mediaPlaybackRequiresUserGesture = false 
                        // Pake User-Agent Chrome Android biar gak dicurigai bot
                        settings.userAgentString = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Mobile Safari/537.36"
                        settings.useWideViewPort = true
                        settings.loadWithOverviewMode = true
                        
                        webViewClient = object : WebViewClient() {
                            override fun shouldInterceptRequest(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): android.webkit.WebResourceResponse? {
                                val url = request?.url.toString()
                                if (url.contains(".m3u8") && !url.contains(".ts")) {
                                    m3u8Url = url
                                }
                                return super.shouldInterceptRequest(view, request)
                            }
                        }
                        loadUrl(embedUrl)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(if (m3u8Url == null) 1f else 0f) // Kalo udah dapet, sembunyiin
            )
        }

        // Tampilin Video kalo m3u8 udah dapet
        if (m3u8Url != null) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = true
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Tombol Back
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopEnd) // Pindah ke kanan atas biar gak nutupin teks debug
                .padding(top = 40.dp, end = 16.dp)
                .background(GlassWhite, RoundedCornerShape(50))
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
        }
    }
}
