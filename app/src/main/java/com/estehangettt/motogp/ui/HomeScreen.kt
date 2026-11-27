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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.estehangettt.motogp.R
import com.estehangettt.motogp.ui.theme.*

data class CircuitDummy(
    val name: String,
    val country: String
)

@Composable
fun HomeScreen() {
    val dummyCircuits = listOf(
        CircuitDummy("Pertamina Mandalika", "Indonesia"),
        CircuitDummy("Algarve International", "Portugal"),
        CircuitDummy("Lusail International", "Qatar"),
        CircuitDummy("Circuit of The Americas", "USA")
    )

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
                modifier = Modifier.height(40.dp)
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

        // Grid Sirkuit
        Text(
            text = "Season 2026",
            color = TextGray,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(dummyCircuits) { circuit ->
                CircuitCard(circuit)
            }
        }
    }
}

@Composable
fun CircuitCard(circuit: CircuitDummy) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF2A2A2A), Color(0xFF111111))
                )
            )
            .bouncyClickable { /* Nanti buka ke halaman detail */ }
    ) {
        // Overlay Gradient Gelap
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
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
                text = circuit.country.uppercase(),
                color = MotoGPRed,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = circuit.name,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2
            )
        }
    }
}
