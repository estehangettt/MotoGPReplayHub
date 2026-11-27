package com.estehangettt.motogp.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.view.WindowManager
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.ui.PlayerView
import com.estehangettt.motogp.data.scraper.MotoMundoStreamExtractor
import com.estehangettt.motogp.ui.theme.MotoGPRed
import com.estehangettt.motogp.ui.theme.GlassWhite

@Composable
fun PlayerScreen(embedUrl: String, onBackClick: () -> Unit) {
    var m3u8Url by remember { mutableStateOf<String?>(null) }
    var debugStatus by remember { mutableStateOf("Mengekstrak Link Video...") }
    
    val context = LocalContext.current
    val exoPlayer = remember { ExoPlayer.Builder(context).build().apply { playWhenReady = true } }
    val extractor = remember { MotoMundoStreamExtractor() }

    // 1. Ekstrak m3u8
    LaunchedEffect(embedUrl) {
        val url = extractor.extractM3u8(embedUrl)
        if (url != null) {
            m3u8Url = url
        } else {
            debugStatus = "Gagal mengekstrak link .m3u8."
        }
    }

    // 2. Putar Video
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

    // 3. Handle Orientasi & Fullscreen & KEEP SCREEN ON
    DisposableEffect(Unit) {
        val activity = context.findActivity()
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        
        val window = activity?.window
        if (window != null) {
            // KUNCI LAYAR BIAR GAK MATI
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            insetsController.hide(WindowInsetsCompat.Type.systemBars())
        }

        onDispose {
            exoPlayer.release()
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            if (window != null) {
                // LEPAS KUNCI LAYAR
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                WindowCompat.getInsetsController(window, window.decorView).show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (m3u8Url != null) {
            AndroidView(
                factory = { ctx -> PlayerView(ctx).apply { player = exoPlayer; useController = true } },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(color = MotoGPRed)
                Spacer(modifier = Modifier.height(16.dp))
                Text(debugStatus, color = Color.White)
            }
        }

        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 24.dp, start = 24.dp)
                .background(GlassWhite, RoundedCornerShape(50))
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
        }
    }
}

fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}
