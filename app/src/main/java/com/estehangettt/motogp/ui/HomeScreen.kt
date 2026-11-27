package com.estehangettt.motogp.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.estehangettt.motogp.R
import com.estehangettt.motogp.data.EventResponse
import com.estehangettt.motogp.ui.theme.*

@Composable
fun HomeScreen(viewModel: HomeViewModel = viewModel(), onEventClick: (EventResponse) -> Unit) {
    val events by viewModel.events.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MotoGPBlack)
    ) {
        // Header / Logo
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 40.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_motogp_logo),
                contentDescription = "MotoGP Logo",
                modifier = Modifier.height(32.dp),
                colorFilter = ColorFilter.tint(Color.White)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Replay Hub",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = "CURRENT SEASON",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp, bottom = 12.dp)
        )

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MotoGPRed)
            }
        } else {
            // Pakai LazyColumn (List Vertikal) biar mirip VideoPass
            LazyColumn(
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(events) { event ->
                    VideoPassCard(event, onClick = { onEventClick(event) })
                }
            }
        }
    }
}

@Composable
fun VideoPassCard(event: EventResponse, onClick: () -> Unit) {
    // Ambil URL dari API
    val topImageUrl = event.assets?.find { it.type == "TOP" || it.type == "BACKGROUND" }?.path
    val flagUrl = event.assets?.find { it.type == "FLAG" }?.path
    val trackImageUrl = event.circuit?.track?.assets?.simple?.path

    // Format tanggal dari API (2026-03-01T00:00:00 -> 01 MAR)
    val dateText = event.date_start?.substring(5, 10)?.replace("-", " ") ?: "TBA"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(180.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1A1A1A))
            .bouncyClickable { onClick() }
    ) {
        // 1. Background Image (Foto Sirkuit/Pembalap)
        if (topImageUrl != null) {
            AsyncImage(
                model = topImageUrl,
                contentDescription = "Event Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // 2. Gradient Overlay (Biar teks kebaca)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f))
                    )
                )
        )

        // 3. Track Outline (Garis Putih di tengah)
        if (trackImageUrl != null) {
            AsyncImage(
                model = trackImageUrl,
                contentDescription = "Track Map",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                colorFilter = ColorFilter.tint(Color.White.copy(alpha = 0.6f))
            )
        }

        // 4. Top Left: Mini MotoGP Logo & "VIDEO PASS" Text
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_motogp_logo),
                contentDescription = "Logo",
                modifier = Modifier.height(16.dp),
                colorFilter = ColorFilter.tint(Color.White)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "VIDEOPASS",
                color = Color.White,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // 5. Top Right: Bendera (Flag)
        if (flagUrl != null) {
            AsyncImage(
                model = flagUrl,
                contentDescription = "Flag",
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .size(24.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
        }

        // 6. Bottom: Judul Sirkuit & Tanggal
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.circuit?.name?.uppercase() ?: event.name,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = event.circuit?.country ?: "UNKNOWN",
                    color = TextGray,
                    fontSize = 12.sp
                )
            }
            
            // Tanggal ala VideoPass (contoh: 01 - 03 MAR)
            Text(
                text = "📅 $dateText",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
