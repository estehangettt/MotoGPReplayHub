package com.estehangettt.motogp.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
fun HomeScreen(viewModel: HomeViewModel = viewModel(), onEventClick: (EventResponse, Int) -> Unit) {
    val events by viewModel.events.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().background(MotoGPBlack)
    ) {
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
            LazyColumn(
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Pake itemsIndexed biar dapet nomor ronde (index + 1)
                itemsIndexed(events) { index, event ->
                    VideoPassCard(event, round = index + 1, onClick = { onEventClick(event, index + 1) })
                }
            }
        }
    }
}

@Composable
fun VideoPassCard(event: EventResponse, round: Int, onClick: () -> Unit) {
    val topImageUrl = event.assets?.find { it.type == "TOP" || it.type == "BACKGROUND" }?.path
    val flagUrl = event.assets?.find { it.type == "FLAG" }?.path
    val trackImageUrl = event.circuit?.track?.assets?.simple?.path
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
        if (topImageUrl != null) {
            AsyncImage(
                model = topImageUrl, contentDescription = "Event Image",
                contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()
            )
        }

        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f)))
            )
        )

        if (trackImageUrl != null) {
            AsyncImage(
                model = trackImageUrl, contentDescription = "Track Map",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize().padding(24.dp),
                colorFilter = ColorFilter.tint(Color.White.copy(alpha = 0.6f))
            )
        }

        Row(
            modifier = Modifier.align(Alignment.TopStart).padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_motogp_logo),
                contentDescription = "Logo", modifier = Modifier.height(16.dp),
                colorFilter = ColorFilter.tint(Color.White)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("VIDEOPASS", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
        }

        if (flagUrl != null) {
            AsyncImage(
                model = flagUrl, contentDescription = "Flag",
                modifier = Modifier.align(Alignment.TopEnd).padding(12.dp).size(24.dp).clip(RoundedCornerShape(4.dp))
            )
        }

        Row(
            modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.circuit?.name?.uppercase() ?: event.name,
                    color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black, lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(event.circuit?.country ?: "UNKNOWN", color = TextGray, fontSize = 12.sp)
            }
            Text("📅 $dateText", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
