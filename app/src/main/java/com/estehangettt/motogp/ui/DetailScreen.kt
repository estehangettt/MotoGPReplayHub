package com.estehangettt.motogp.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.estehangettt.motogp.data.EventResponse
import com.estehangettt.motogp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(event: EventResponse, onBackClick: () -> Unit) {
    val topImageUrl = event.assets?.find { it.type == "TOP" || it.type == "BACKGROUND" }?.path
    val flagUrl = event.assets?.find { it.type == "FLAG" }?.path
    val trackImageUrl = event.circuit?.track?.assets?.simple?.path

    Box(modifier = Modifier.fillMaxSize().background(MotoGPBlack)) {
        // 1. Background Image Full Screen (Blur dikit)
        if (topImageUrl != null) {
            AsyncImage(
                model = topImageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                alpha = 0.3f
            )
            // Gradient Overlay biar bawahnya item
            Box(
                modifier = Modifier.fillMaxSize().background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, MotoGPBlack, MotoGPBlack)
                    )
                )
            )
        }

        // 2. Konten Utama
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            // Tombol Back
            Spacer(modifier = Modifier.height(32.dp))
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.background(GlassWhite, RoundedCornerShape(50))
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }

            Spacer(modifier = Modifier.weight(0.3f))

            // 3. Kartu Info Sirkuit (Frosted Glass)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassmorphism(cornerRadius = 24)
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (flagUrl != null) {
                        AsyncImage(
                            model = flagUrl,
                            contentDescription = "Flag",
                            modifier = Modifier.size(32.dp).clip(RoundedCornerShape(4.dp))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    Text(
                        text = event.circuit?.country?.uppercase() ?: "",
                        color = MotoGPRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = event.circuit?.name ?: event.name,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    lineHeight = 28.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color.White.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(16.dp))

                // Info Statistik (Panjang & Kapasitas)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("PANJANG", color = TextGray, fontSize = 10.sp)
                        Text("${event.circuit?.track?.lenght_units?.kiloMeters ?: "-"} km", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("KAPASITAS", color = TextGray, fontSize = 10.sp)
                        Text("${event.circuit?.capacity ?: "-"}", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4. Tombol Sesi Balapan (Dummy dulu, nanti disambung ke MotoMundo)
            Text("PILIH SESI", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            
            val sessions = listOf("FP1", "FP2", "Q1", "Q2", "SPRINT", "RACE")
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.height(150.dp) // Biar bisa discroll
            ) {
                items(sessions) { sesi ->
                    Box(
                        modifier = Modifier
                            .height(60.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF222222))
                            .bouncyClickable { /* Nanti buka Video Player */ },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(sesi, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
