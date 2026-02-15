package com.pedrorau.callfilter.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pedrorau.callfilter.ui.theme.Primary
import com.pedrorau.callfilter.ui.theme.TextSecondary

private data class OnboardingPage(
    val title: String,
    val description: String,
    val icon: String
)

private val pages = listOf(
    OnboardingPage(
        title = "Private by Design",
        description = "All filtering happens offline on your device. Your call data never leaves your phone.",
        icon = "privacy"
    ),
    OnboardingPage(
        title = "Set and Forget",
        description = "Configure your preferences once. We'll handle the spam and telemarketers while you focus on what matters.",
        icon = "automation"
    ),
    OnboardingPage(
        title = "Enable Protection",
        description = "CallFilter needs to be set as your call screening app to block unwanted calls automatically.",
        icon = "permissions"
    )
)

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onComplete: () -> Unit,
    onSkip: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "CallFilter",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Primary
            )
            TextButton(onClick = onSkip) {
                Text("Skip", color = Primary.copy(alpha = 0.8f))
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedContent(targetState = state.currentPage) { page ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val currentPage = pages[page]

                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Primary.copy(alpha = 0.08f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (page) {
                                0 -> "\uD83D\uDD12"
                                1 -> "\u2699\uFE0F"
                                else -> "\uD83D\uDEE1\uFE0F"
                            },
                            fontSize = 64.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = currentPage.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = currentPage.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(state.totalPages) { index ->
                    Box(
                        modifier = Modifier
                            .then(
                                if (index == state.currentPage) {
                                    Modifier.width(24.dp).height(8.dp)
                                } else {
                                    Modifier.size(8.dp)
                                }
                            )
                            .clip(CircleShape)
                            .background(
                                if (index == state.currentPage) Primary
                                else Primary.copy(alpha = 0.2f)
                            )
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    if (state.currentPage == state.totalPages - 1) {
                        onComplete()
                    } else {
                        viewModel.nextPage()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text(
                    text = if (state.currentPage == state.totalPages - 1) {
                        "Enable call protection"
                    } else {
                        "Next"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}
