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
import com.pedrorau.callfilter.resources.*
import com.pedrorau.callfilter.ui.theme.*
import org.jetbrains.compose.resources.stringResource

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
            stringResource(Res.string.home_status_active_title),
            stringResource(Res.string.home_status_active_desc)
        )
        SystemState.INCOMPLETE_SETUP -> Triple(
            WarningAmber,
            stringResource(Res.string.home_status_incomplete_title),
            stringResource(Res.string.home_status_incomplete_desc)
        )
        SystemState.NOT_CONFIGURED -> Triple(
            ErrorRed,
            stringResource(Res.string.home_status_not_configured_title),
            stringResource(Res.string.home_status_not_configured_desc)
        )
        SystemState.POTENTIALLY_INCOMPATIBLE -> Triple(
            WarningAmber,
            stringResource(Res.string.home_status_incompatible_title),
            stringResource(Res.string.home_status_incompatible_desc)
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
                text = stringResource(Res.string.app_name),
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
                        text = stringResource(Res.string.home_samsung_warning),
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
                            text = stringResource(Res.string.home_battery_message),
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
                                stringResource(Res.string.home_battery_action),
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
                    text = stringResource(Res.string.home_configure_rules),
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
                    text = stringResource(Res.string.home_how_it_works),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            }
        }
    }
}
