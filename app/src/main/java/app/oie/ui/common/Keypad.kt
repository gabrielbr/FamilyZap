package app.oie.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import app.oie.R
import app.oie.logic.Pin
import app.oie.ui.text.str
import app.oie.ui.theme.OTokens
import app.oie.ui.theme.OieTheme
import app.oie.ui.theme.OieType

/** Six dots and a 72dp keypad (prototype screens 09 and 11). */
@Composable
fun PinPad(entered: String, onChange: (String) -> Unit) {
    val c = OieTheme.colors
    val tx = str
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(OTokens.Spacing.space4)) {
        Row(
            Modifier.semantics { contentDescription = tx.pinDots(entered.length, Pin.LENGTH) },
            horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space4),
        ) {
            repeat(Pin.LENGTH) { i ->
                Box(
                    Modifier.size(OTokens.Spacing.space4).clip(CircleShape)
                        .background(if (i < entered.length) c.ink else c.page)
                        .border(BorderStroke(OTokens.Spacing.space1 / 2, c.ink), CircleShape),
                )
            }
        }
        val rows = listOf("123", "456", "789", " 0<")
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space8)) {
                row.forEach { k ->
                    when (k) {
                        ' ' -> Spacer(Modifier.size(OTokens.Size.record))
                        '<' -> Key(onClick = { onChange(entered.dropLast(1)) }, label = tx.erase) { OIcon(R.drawable.gg_ic_back) }
                        else -> Key(onClick = { if (entered.length < Pin.LENGTH) onChange(entered + k) }, label = k.toString()) {
                            Text(k.toString(), style = OieType.title, color = c.ink)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Key(onClick: () -> Unit, label: String, content: @Composable () -> Unit) {
    val c = OieTheme.colors
    val tx = str
    Box(
        Modifier.size(OTokens.Size.record).clip(CircleShape).background(c.surface)
            .border(BorderStroke(OTokens.Size.hairline, c.line), CircleShape)
            .clickable(role = Role.Button, onClickLabel = label, onClick = onClick)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) { content() }
}
