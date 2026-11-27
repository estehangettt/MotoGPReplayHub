package com.estehangettt.motogp.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
fun HomeScreen(viewModel: HomeViewModel = viewModel()) {
    val events by viewModel.events.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MotoGPBlack)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        
        // Header / Logo
        Row(verticalAlignment = Alignment.CenterVertically) {
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

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Season 2026",
            color = TextGray,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MotoGPRed)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(events) { event ->
                    CircuitCard(event)
                }
            }
        }
    }
}

@Composable
fun CircuitCard(event: EventResponse) {
    // Ambil URL gambar layout sirkuit dari API
    val trackImageUrl = event.circuit?.track?.assets?.simple?.path

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1A1A1A))
            .bouncyClickable { /* Nanti buka ke halaman detail */ }
    ) {
        // Gambar Layout Sirkuit sebagai Background
        if (trackImageUrl != null) {
            AsyncImage(
                model = trackImageUrl,
                contentDescription = "Track Layout",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().padding(16.dp),
                // Bikin gambarnya agak transparan/abu-abu biar teks kebaca
                alpha = 0.3f 
            )
        }

        // Overlay Gradient Gelap
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f))
                    )
                )
        )
        
        // Teks
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
        ) {
            Text(
                text = event.circuit?.country?.uppercase() ?: "UNKNOWN",
                color = MotoGPRed,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = event.circuit?.name ?: event.name,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                lineHeight = 16.sp
            )
        }
    }
}
