package org.delcom.pam_2026_ifs23002_proyek1_fe.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.delcom.pam_2026_ifs23002_proyek1_fe.R
import org.delcom.pam_2026_ifs23002_proyek1_fe.ui.theme.EthnoGold

@Composable
fun RippleLoading(
    modifier: Modifier = Modifier,
    color: Color = EthnoGold,
    imageSize: Dp = 80.dp,
    circleSize: Dp = 40.dp,
    maxSize: Dp = 160.dp,
    animationDuration: Int = 2000
) {
    val density = LocalDensity.current
    val circleSizePx = with(density) { circleSize.toPx() }
    val maxSizePx = with(density) { maxSize.toPx() }

    Box(
        modifier = modifier.size(maxSize + 40.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.logo_karya_budaya),
            contentDescription = "Logo",
            modifier = Modifier.size(imageSize)
        )

        repeat(3) { index ->
            val infiniteTransition = rememberInfiniteTransition(label = "ripple")

            val size by infiniteTransition.animateFloat(
                initialValue = circleSizePx,
                targetValue = maxSizePx,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = animationDuration, easing = LinearOutSlowInEasing),
                    initialStartOffset = StartOffset((animationDuration / 3) * index)
                ), label = "size"
            )

            val alpha by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = animationDuration, easing = LinearOutSlowInEasing),
                    initialStartOffset = StartOffset((animationDuration / 3) * index)
                ), label = "alpha"
            )

            Box(
                modifier = Modifier
                    .size(with(density) { size.toDp() })
                    .border(width = 1.5.dp, color = color.copy(alpha = alpha), shape = CircleShape)
            )
        }
    }
}

@Composable
fun LoadingUI() {
    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        RippleLoading()
    }
}
