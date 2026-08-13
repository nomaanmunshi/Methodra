package io.methodra.app.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.methodra.app.design.MethodraColors
import kotlin.math.cos
import kotlin.math.sin

private const val FinalStage = 8

@Composable
fun StoneOnboarding(
    reduceMotion: Boolean = false,
    hapticsEnabled: Boolean = true,
    onComplete: () -> Unit
) {
    var stage by remember { mutableIntStateOf(0) }
    val haptics = LocalHapticFeedback.current
    val revealed = stage >= FinalStage

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MethodraColors.Obsidian)
            .safeDrawingPadding()
            .padding(horizontal = 28.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onComplete) {
                Text("Skip", color = MethodraColors.Muted)
            }
        }

        Spacer(Modifier.weight(0.45f))

        Text(
            text = if (revealed) "A method is a decision made concrete." else "Good advice is everywhere.\nA method is what turns it into action.",
            color = MethodraColors.Bone,
            fontSize = if (revealed) 26.sp else 28.sp,
            lineHeight = 36.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(40.dp))

        FractureStone(
            stage = stage,
            modifier = Modifier
                .size(280.dp)
                .clickable(enabled = !revealed) {
                    stage++
                    if (hapticsEnabled) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
        )

        Spacer(Modifier.height(34.dp))

        AnimatedVisibility(
            visible = !revealed,
            enter = if (reduceMotion) androidx.compose.animation.EnterTransition.None else fadeIn(),
            exit = if (reduceMotion) androidx.compose.animation.ExitTransition.None else fadeOut()
        ) {
            Text(
                "Tap to uncover your first method",
                color = MethodraColors.Muted,
                fontSize = 15.sp
            )
        }

        AnimatedVisibility(
            visible = revealed,
            enter = if (reduceMotion) androidx.compose.animation.EnterTransition.None else fadeIn()
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "You do not need a perfect reset.\nYou need a method that survives real life.",
                    color = MethodraColors.Bone,
                    textAlign = TextAlign.Center,
                    fontSize = 18.sp,
                    lineHeight = 27.sp
                )
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = onComplete,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MethodraColors.Amber,
                        contentColor = MethodraColors.Obsidian
                    )
                ) {
                    Text("Find my first method", fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(Modifier.weight(0.55f))
    }
}

@Composable
private fun FractureStone(stage: Int, modifier: Modifier = Modifier) {
    val progress = stage.coerceIn(0, FinalStage) / FinalStage.toFloat()

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension * 0.34f

        if (progress > 0f) {
            drawCircle(
                color = MethodraColors.Amber.copy(alpha = 0.08f + progress * 0.14f),
                radius = radius * (0.75f + progress * 0.25f),
                center = center
            )
        }

        val stone = Path().apply {
            moveTo(center.x - radius * 0.62f, center.y - radius * 0.82f)
            lineTo(center.x + radius * 0.48f, center.y - radius * 0.9f)
            lineTo(center.x + radius * 0.88f, center.y - radius * 0.24f)
            lineTo(center.x + radius * 0.68f, center.y + radius * 0.72f)
            lineTo(center.x - radius * 0.14f, center.y + radius * 0.96f)
            lineTo(center.x - radius * 0.84f, center.y + radius * 0.44f)
            lineTo(center.x - radius * 0.9f, center.y - radius * 0.26f)
            close()
        }
        drawPath(stone, MethodraColors.ElevatedStone)
        drawPath(stone, MethodraColors.Muted.copy(alpha = 0.22f), style = Stroke(width = 2f))

        val crackCount = (progress * 11).toInt()
        repeat(crackCount) { index ->
            val angle = -2.5 + index * 0.58
            val length = radius * (0.28f + (index % 3) * 0.12f + progress * 0.25f)
            val start = Offset(
                center.x + cos(angle).toFloat() * radius * 0.08f,
                center.y + sin(angle).toFloat() * radius * 0.08f
            )
            val end = Offset(
                start.x + cos(angle).toFloat() * length,
                start.y + sin(angle).toFloat() * length
            )
            drawLine(
                color = MethodraColors.Amber.copy(alpha = 0.28f + progress * 0.55f),
                start = start,
                end = end,
                strokeWidth = 2.2f
            )
        }

        if (stage >= FinalStage) {
            val coreSize = radius * 0.72f
            drawRect(
                color = MethodraColors.Amber,
                topLeft = Offset(center.x - coreSize / 2f, center.y - coreSize / 2f),
                size = Size(coreSize, coreSize)
            )
            drawRect(
                color = MethodraColors.Bone.copy(alpha = 0.65f),
                topLeft = Offset(center.x - coreSize * 0.22f, center.y - coreSize * 0.22f),
                size = Size(coreSize * 0.44f, coreSize * 0.44f)
            )
        } else if (stage > 1) {
            repeat(stage.coerceAtMost(8)) { index ->
                val angle = index * 0.9 + 0.4
                val dustRadius = radius * (1.02f + (index % 2) * 0.12f)
                drawCircle(
                    color = MethodraColors.Muted.copy(alpha = 0.18f + progress * 0.12f),
                    radius = 2.2f + (index % 3),
                    center = Offset(
                        center.x + cos(angle).toFloat() * dustRadius,
                        center.y + sin(angle).toFloat() * dustRadius
                    )
                )
            }
        }
    }
}
