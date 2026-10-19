package com.pedrorau.callfilter.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pedrorau.callfilter.model.SystemState
import com.pedrorau.callfilter.ui.theme.*

@Composable
fun HomeScreen(
    state: HomeState,
    onConfigureRules: () -> Unit,
    onHowItWorks: () -> Unit,
    onRequestBatteryOptimization: () -> Unit = {}
) {
    val (statusColor, statusTitle, statusDescription) = when (state.systemState) {
        SystemState.PROTECTION_ACTIVE -> Triple(
            SuccessGreen,
            "Protection Active",
            "CallFilter is protecting you from unwanted calls offline."
        )
        SystemState.INCOMPLETE_SETUP -> Triple(
            WarningAmber,
            "Incomplete Setup",
            "Some settings need attention for full protection."
        )
        SystemState.NOT_CONFIGURED -> Triple(
            ErrorRed,
            "Not Configured",
            "Enable call screening to start blocking unwanted calls."
        )
        SystemState.POTENTIALLY_INCOMPATIBLE -> Triple(
            WarningAmber,
            "Possibly Incompatible",
            "Call screening is enabled, but your device may not support third-party call blocking."
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "CallFilter",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .size(112.dp)
                    .clip(CircleShape)
                    .background(statusColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(statusColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (state.systemState) {
                            SystemState.PROTECTION_ACTIVE -> "\uD83D\uDEE1\uFE0F"
                            SystemState.INCOMPLETE_SETUP,
                            SystemState.POTENTIALLY_INCOMPATIBLE -> "\u26A0\uFE0F"
                            SystemState.NOT_CONFIGURED -> "\u274C"
                        },
                        fontSize = 36.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = statusTitle,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = statusColor
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = statusDescription,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 48.dp)
            )

            if (state.systemState == SystemState.POTENTIALLY_INCOMPATIBLE) {
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = WarningAmber.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Some Samsung devices with Android 12+ do not allow third-party apps to filter calls. " +
                                "If you receive a call and it is not blocked, this device is not compatible.",
                        style = MaterialTheme.typography.bodySmall,
                        color = WarningAmber,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            if (state.showBatteryOptimization &&
                (state.systemState == SystemState.PROTECTION_ACTIVE || state.systemState == SystemState.POTENTIALLY_INCOMPATIBLE)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Primary.copy(alpha = 0.08f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Some phones may stop background call filtering. " +
                                    "Allow unrestricted battery usage for better reliability.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = onRequestBatteryOptimization,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Allow unrestricted usage",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onConfigureRules,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text(
                    text = "Configure Rules",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            }

            OutlinedButton(
                onClick = onHowItWorks,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Primary,
                    containerColor = Primary.copy(alpha = 0.1f)
                ),
                border = null
            ) {
                Text(
                    text = "How it works",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            }
        }
    }
}
