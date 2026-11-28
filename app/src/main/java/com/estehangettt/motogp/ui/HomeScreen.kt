package com.estehangettt.motogp.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.estehangettt.motogp.R
import com.estehangettt.motogp.data.EventResponse
import com.estehangettt.motogp.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: HomeViewModel = viewModel(), onEventClick: (EventResponse, Int) -> Unit) {
    val events by viewModel.events.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedEvent by viewModel.selectedEvent.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    
    // Warna dinamis berdasarkan tab (MotoGP/Moto2/Moto3)
    val accents = LocalAccent.current
    val activeColor = when(selectedCategory) {
        "moto2" -> accents.moto2
        "moto3" -> accents.moto3
        else -> accents.motogp
    }

    val pagerState = rememberPagerState(pageCount = { events.size })
    val scope = rememberCoroutineScope()

    // Sinkronisasi Pager dengan ViewModel
    LaunchedEffect(pagerState.currentPage) {
        if (events.isNotEmpty()) {
            viewModel.setEvent(events[pagerState.currentPage])
        }
    }

    Scaffold(
        bottomBar = { BottomNavBar(activeColor) },
        containerColor = DarkBg
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = activeColor)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // --- TOP BAR ---
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_motogp_logo),
                        contentDescription = "Logo",
                        modifier = Modifier.height(32.dp),
                        colorFilter = ColorFilter.tint(Color.White)
                    )
                    IconButton(onClick = { /* Fitur Search nanti */ }, modifier = Modifier.background(GlassWhite, RoundedCornerShape(50))) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
                    }
                }

                // --- HERO BANNER ---
                if (events.isNotEmpty()) {
                    Box(modifier = Modifier.height(300.dp)) {
                        HorizontalPager(state = pagerState) { page ->
                            val event = events[page]
                            HeroCard(event = event)
                        }
                        
                        // Dots Indicator
                        Row(
                            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            repeat(events.size) { iteration ->
                                val color = if (pagerState.currentPage == iteration) activeColor else Color.White.copy(alpha = 0.4f)
                                Box(
                                    modifier = Modifier
                                        .width(if (pagerState.currentPage == iteration) 20.dp else 12.dp)
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(color)
                                )
                            }
                        }
                    }
                }

                // --- DROPDOWN KACA (Season & GP) ---
                Column(modifier = Modifier.padding(16.dp)) {
                    DropdownGlass(text = "Season: 2026", icon = Icons.Default.ArrowDropDown, onClick = {})
                    Spacer(modifier = Modifier.height(10.dp))
                    selectedEvent?.let {
                        DropdownGlass(
                            text = "${it.name.take(3).uppercase()} · ${it.circuit?.name ?: it.name}",
                            icon = Icons.Default.ArrowDropDown,
                            onClick = {}
                        )
                    }
                }

                // --- TABS (MotoGP/Moto2/Moto3) ---
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CategoryTab("MotoGP", "motogp", selectedCategory, activeColor) { viewModel.setCategory("motogp") }
                    CategoryTab("Moto2", "moto2", selectedCategory, activeColor) { viewModel.setCategory("moto2") }
                    CategoryTab("Moto3", "moto3", selectedCategory, activeColor) { viewModel.setCategory("moto3") }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- GRID SESI ---
                val sessions = listOf("FP1", "PR", "FP2", "Q1", "Q2", "SPRINT", "RACE")
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(sessions) { sesi ->
                        SessionCard(
                            sesi = sesi,
                            event = selectedEvent,
                            activeColor = activeColor,
                            onClick = {
                                if (selectedEvent != null) {
                                    // Nanti kita sambungin ke Scraper & Detail Screen
                                    onEventClick(selectedEvent!!, 1)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HeroCard(event: EventResponse) {
    val topImageUrl = event.assets?.find { it.type == "TOP" || it.type == "BACKGROUND" }?.path
    Box(modifier = Modifier.fillMaxSize()) {
        if (topImageUrl != null) {
            AsyncImage(
                model = topImageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        // Gradient Overlay
        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(colors = listOf(Color.Transparent, DarkBg))
            )
        )
        
        // Teks
        Column(
            modifier = Modifier.align(Alignment.BottomStart).padding(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.width(4.dp).height(24.dp).background(LocalAccent.current.motogp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = event.circuit?.name ?: event.name,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    fontStyle = FontStyle.Italic,
                    maxLines = 2
                )
            }
            Text(
                text = "${event.name} · ${event.date_start?.take(10) ?: ""}",
                color = TextMuted,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun DropdownGlass(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .glassmorphism(cornerRadius = 16)
            .bouncyClickable { onClick() }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(text, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Icon(icon, contentDescription = null, tint = Color.White)
        }
    }
}

@Composable
fun CategoryTab(title: String, id: String, selectedId: String, activeColor: Color, onClick: () -> Unit) {
    val isSelected = id == selectedId
    Box(
        modifier = Modifier
            .weight(1f)
            .height(44.dp)
            .clip(RoundedCornerShape(50))
            .background(if (isSelected) activeColor else GlassWhite)
            .bouncyClickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold
        )
    }
}

@Composable
fun SessionCard(sesi: String, event: EventResponse?, activeColor: Color, onClick: () -> Unit) {
    val imageUrl = event?.assets?.find { it.type == "TOP" }?.path
    Box(
        modifier = Modifier
            .height(120.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF131317))
            .bouncyClickable { onClick() }
    ) {
        if (imageUrl != null) {
            AsyncImage(
                model = imageUrl, contentDescription = null,
                contentScale = ContentScale.Crop,
                alpha = 0.3f,
                modifier = Modifier.fillMaxSize()
            )
        }
        Box(modifier = Modifier.fillMaxSize().background(Brush.linearGradient(listOf(activeColor.copy(alpha = 0.4f), Color.Transparent))))

        Text(
            text = sesi,
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.Black,
            fontStyle = FontStyle.Italic,
            modifier = Modifier.align(Alignment.TopStart).padding(12.dp)
        )
        Text(
            text = "Hasil & info",
            color = TextMuted,
            fontSize = 10.sp,
            modifier = Modifier.align(Alignment.BottomStart).padding(12.dp)
        )
        // Tombol Arrow
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp)
                .size(28.dp)
                .clip(RoundedCornerShape(50))
                .background(activeColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.ArrowForwardIos, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
        }
    }
}

@Composable
fun BottomNavBar(activeColor: Color) {
    // Floating Bottom Nav
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(64.dp)
            .glassmorphism(cornerRadius = 32)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem("Home", true, activeColor)
            BottomNavItem("Calendar", false, activeColor)
            BottomNavItem("Standings", false, activeColor)
            BottomNavItem("Library", false, activeColor)
            BottomNavItem("More", false, activeColor)
        }
    }
}

@Composable
fun BottomNavItem(label: String, isActive: Boolean, activeColor: Color) {
    // Sederhana dulu, nanti kita tambahin Icon
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = if (isActive) activeColor else TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}
