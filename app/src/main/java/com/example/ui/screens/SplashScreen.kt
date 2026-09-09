package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.BuildConfig
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.AppStrings
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.MintGreenAccent
import com.example.ui.theme.DarkGreenCard
import com.example.ui.theme.PrimaryEmerald
import com.example.ui.theme.PrimaryEmeraldDark
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
  language: AppLanguage,
  onProceed: () -> Unit
) {
  var startAnimation by remember { mutableStateOf(false) }

  val alphaAnim by animateFloatAsState(
    targetValue = if (startAnimation) 1f else 0f,
    animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
    label = "splashAlpha"
  )

  val scaleAnim by animateFloatAsState(
    targetValue = if (startAnimation) 1f else 0.85f,
    animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
    label = "splashScale"
  )

  LaunchedEffect(Unit) {
    startAnimation = true
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          colors = listOf(
            Color(0xFF041410),
            Color(0xFF0A241E),
            Color(0xFF0F382E)
          )
        )
      )
      .padding(24.dp)
      .testTag("splash_screen_root"),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
      modifier = Modifier
        .fillMaxWidth()
        .alpha(alphaAnim)
        .scale(scaleAnim)
    ) {
      // App & Brand Emblem (White Car Logo without circle)
      Icon(
        imageVector = Icons.Default.DirectionsCar,
        contentDescription = "Car Hisab Logo",
        tint = Color.White,
        modifier = Modifier.size(100.dp)
      )

      Spacer(modifier = Modifier.height(24.dp))

      // App Title
      Text(
        text = AppStrings.appTitle(language),
        color = Color.White,
        fontSize = 32.sp,
        fontWeight = FontWeight.ExtraBold,
        textAlign = TextAlign.Center
      )

      Text(
        text = "CAR HISAB",
        color = PrimaryEmeraldDark,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 4.sp,
        modifier = Modifier.padding(top = 4.dp)
      )

      Spacer(modifier = Modifier.height(12.dp))

      // Tagline
      Text(
        text = AppStrings.appTagline(language),
        color = Color(0xFFCBD5E1),
        fontSize = 14.sp,
        textAlign = TextAlign.Center,
        lineHeight = 20.sp,
        modifier = Modifier.padding(horizontal = 20.dp)
      )

      Spacer(modifier = Modifier.height(48.dp))

      // Proceed Button
      Button(
        onClick = onProceed,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = PrimaryEmeraldDark,
          contentColor = Color(0xFF041410)
        ),
        modifier = Modifier
          .fillMaxWidth(0.85f)
          .height(54.dp)
          .testTag("splash_continue_button")
      ) {
        Text(
          text = if (language == AppLanguage.BANGLA) "শুরু করুন" else "Get Started",
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowForward,
          contentDescription = null,
          modifier = Modifier.size(20.dp)
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = "Powered by MMRK Innovations • v${BuildConfig.VERSION_NAME}",
        color = Color(0xFF64748B),
        fontSize = 11.sp,
        textAlign = TextAlign.Center
      )
    }
  }
}
