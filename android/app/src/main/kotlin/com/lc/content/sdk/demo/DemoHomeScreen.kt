package com.lc.content.sdk.demo

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// -----------------------------------------------------------------------------
// Demo UI only. Nothing in this file is required to integrate the SDK.
// -----------------------------------------------------------------------------

private object DemoColors {
    val Background = Color(0xFF0E0F13)
    val Surface = Color(0xFF17191F)
    val Outline = Color(0xFF262932)
    val TextPrimary = Color(0xFFF4F5F7)
    val TextSecondary = Color(0xFF9AA0AC)
    val Accent = Color(0xFFFF4D6D)
    val AccentEnd = Color(0xFFFF8A4C)
    val Success = Color(0xFF34D399)
    val Warning = Color(0xFFFBBF24)
    val Error = Color(0xFFF87171)
}

/** One row of the "integration steps" checklist and where to find it in this project. */
private data class IntegrationStep(val title: String, val file: String)

private val integrationSteps = listOf(
    IntegrationStep("Add SDK repositories", "settings.gradle.kts"),
    IntegrationStep("Add the AAR and its dependencies", "app/build.gradle.kts"),
    IntegrationStep("Set your AdMob Application ID", "AndroidManifest.xml"),
    IntegrationStep("Initialize the SDK", "DemoApplication.kt"),
    IntegrationStep("Ad consent + start", "MainActivity.kt"),
    IntegrationStep("Open the SDK page", "MainActivity.kt"),
)

@Composable
fun DemoHomeScreen(
    status: SdkStatus,
    onOpenDramaHome: () -> Unit,
) {
    MaterialTheme(colorScheme = darkColorScheme(primary = DemoColors.Accent)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DemoColors.Background)
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
            ) {
                Spacer(Modifier.height(32.dp))
                Header()
                Spacer(Modifier.height(28.dp))
                StatusCard(status)
                Spacer(Modifier.height(16.dp))
                StepsCard()
                Spacer(Modifier.height(24.dp))
            }
            OpenButton(
                status = status,
                onClick = onOpenDramaHome,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            )
        }
    }
}

@Composable
private fun Header() {
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(listOf(DemoColors.Accent, DemoColors.AccentEnd))),
        contentAlignment = Alignment.Center,
    ) {
        Text("▶", color = Color.White, fontSize = 22.sp)
    }
    Spacer(Modifier.height(20.dp))
    Text(
        text = "CONTENT SDK",
        color = DemoColors.Accent,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 2.sp,
    )
    Spacer(Modifier.height(6.dp))
    Text(
        text = "Integration Demo",
        color = DemoColors.TextPrimary,
        fontSize = 30.sp,
        fontWeight = FontWeight.Bold,
    )
    Spacer(Modifier.height(8.dp))
    Text(
        text = "The minimum code needed to bring short-drama content into your Android app.",
        color = DemoColors.TextSecondary,
        fontSize = 15.sp,
        lineHeight = 22.sp,
    )
}

@Composable
private fun DemoCard(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(DemoColors.Surface)
            .border(1.dp, DemoColors.Outline, RoundedCornerShape(20.dp))
            .padding(18.dp),
    ) {
        content()
    }
}

@Composable
private fun StatusCard(status: SdkStatus) {
    val (label, detail, color) = when (status) {
        SdkStatus.NotStarted -> Triple("Initialized", "Tap the button below to start the SDK.", DemoColors.TextSecondary)
        SdkStatus.Starting -> Triple("Starting…", "Checking ad consent and booting the SDK.", DemoColors.Warning)
        SdkStatus.Ready -> Triple("Ready", "The SDK is running. SDK pages can be opened.", DemoColors.Success)
        is SdkStatus.Failed -> Triple("Start failed", "${status.code} · ${status.message}", DemoColors.Error)
    }
    val dotColor by animateColorAsState(color, label = "statusDot")

    DemoCard {
        Text("SDK STATUS", color = DemoColors.TextSecondary, fontSize = 11.sp, letterSpacing = 1.5.sp)
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(dotColor),
            )
            Spacer(Modifier.width(10.dp))
            Text(label, color = DemoColors.TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(6.dp))
        Text(detail, color = DemoColors.TextSecondary, fontSize = 13.sp, lineHeight = 19.sp)
    }
}

@Composable
private fun StepsCard() {
    DemoCard {
        Text("INTEGRATION STEPS", color = DemoColors.TextSecondary, fontSize = 11.sp, letterSpacing = 1.5.sp)
        Spacer(Modifier.height(6.dp))
        integrationSteps.forEachIndexed { index, step ->
            Row(
                modifier = Modifier.padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(DemoColors.Accent.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("${index + 1}", color = DemoColors.Accent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(step.title, color = DemoColors.TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(2.dp))
                    Text(step.file, color = DemoColors.TextSecondary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

@Composable
private fun OpenButton(status: SdkStatus, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val starting = status == SdkStatus.Starting
    Button(
        onClick = onClick,
        enabled = !starting,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = DemoColors.Accent,
            disabledContainerColor = DemoColors.Accent.copy(alpha = 0.5f),
        ),
    ) {
        if (starting) {
            CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
        } else {
            Text(
                text = if (status is SdkStatus.Failed) "Retry" else "Open Drama Home",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun DemoHomeScreenPreview() {
    DemoHomeScreen(status = SdkStatus.Ready, onOpenDramaHome = {})
}
