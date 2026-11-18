package app.oie.ui.parent

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import app.oie.logic.QuietPeriod
import app.oie.ui.common.PrimaryButton
import app.oie.ui.common.QuietButton
import app.oie.ui.common.TextLink
import app.oie.ui.setup.Field
import app.oie.ui.text.Strings
import app.oie.ui.text.str
import app.oie.ui.theme.OTokens
import app.oie.ui.theme.OieTheme
import app.oie.ui.theme.OieType
import java.time.DayOfWeek
import java.time.LocalTime


/** Short words for a set of days: "Todos os dias", "Seg a sex", "Sáb e dom", or the list. */
fun daysLabel(tx: Strings, days: Set<DayOfWeek>): String = when {
    days.size == 7 -> tx.everyDay
    days == DayOfWeek.entries.take(5).toSet() -> tx.weekdays
    days == setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY) -> tx.weekend
    days.isEmpty() -> tx.noDays
    else -> days.sorted().joinToString(", ") { tx.dayShort[it.value - 1] }
}

/** "7h às 12h", or "21h às 7h do dia seguinte" for an overnight period. */
fun hoursLabel(tx: Strings, p: QuietPeriod): String =
    tx.hours(tx.clock(p.start.hour, p.start.minute), tx.clock(p.end.hour, p.end.minute), p.end < p.start)

/** Add or edit one quiet-hours period (flow: contacts-quiet-hours.md, step 8). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeriodEditor(initial: QuietPeriod?, onSave: (QuietPeriod) -> Unit, onDelete: (() -> Unit)?, onCancel: () -> Unit) {
    val c = OieTheme.colors
    val tx = str
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var days by remember { mutableStateOf(initial?.days ?: DayOfWeek.entries.take(5).toSet()) }
    var start by remember { mutableStateOf(initial?.start ?: LocalTime.of(13, 0)) }
    var end by remember { mutableStateOf(initial?.end ?: LocalTime.of(18, 0)) }
    var picking by remember { mutableStateOf<String?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(OTokens.Spacing.space4)) {
        Text(if (initial == null) tx.newPeriod else tx.editPeriod, style = OieType.title, color = c.ink)
        Field(tx.name, name, { name = it }, tx.nameHelp)
        Text(tx.days, style = OieType.label, color = c.ink)
        Row(horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space2)) {
            DayOfWeek.entries.forEach { d ->
                val on = d in days
                Box(
                    Modifier.defaultMinSize(minWidth = OTokens.Spacing.space12, minHeight = OTokens.Spacing.space12)
                        .clip(RoundedCornerShape(OTokens.Radius.radiusL))
                        .background(if (on) c.selected else c.surface)
                        .border(OTokens.Size.hairline, if (on) c.ink else c.line, RoundedCornerShape(OTokens.Radius.radiusL))
                        .clickable(role = Role.Checkbox) { days = if (on) days - d else days + d }
                        .semantics { selected = on },
                    contentAlignment = Alignment.Center,
                ) { Text(tx.dayShort[d.value - 1], style = OieType.label, color = if (on) c.onSelected else c.ink) }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space4), verticalAlignment = Alignment.CenterVertically) {
            QuietButton(tx.startsAt(tx.clock(start.hour, start.minute)), { picking = "start" }, Modifier.height(OTokens.Size.button))
            QuietButton(tx.endsAt(tx.clock(end.hour, end.minute)), { picking = "end" }, Modifier.height(OTokens.Size.button))
            if (end < start) Text(tx.nextDay, style = OieType.caption, color = c.muted)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space4), verticalAlignment = Alignment.CenterVertically) {
            PrimaryButton(tx.save, {
                onSave(QuietPeriod(name.trim(), days, start, end, on = initial?.on ?: true))
            }, enabled = name.isNotBlank() && days.isNotEmpty() && start != end)
            QuietButton(tx.cancel, onCancel, Modifier.height(OTokens.Size.button))
            if (onDelete != null) TextLink(tx.deletePeriod, onDelete)
        }
    }

    picking?.let { which ->
        val now = if (which == "start") start else end
        val tp = rememberTimePickerState(initialHour = now.hour, initialMinute = now.minute, is24Hour = true)
        AlertDialog(
            onDismissRequest = { picking = null },
            confirmButton = { TextLink("OK", { val t = LocalTime.of(tp.hour, tp.minute); if (which == "start") start = t else end = t; picking = null }) },
            dismissButton = { TextLink(tx.cancel, { picking = null }) },
            containerColor = c.surface,
            text = {
                TimePicker(
                    tp,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = c.page, selectorColor = c.mint, containerColor = c.surface,
                        timeSelectorSelectedContainerColor = c.selected, timeSelectorSelectedContentColor = c.onSelected,
                        timeSelectorUnselectedContainerColor = c.page, timeSelectorUnselectedContentColor = c.ink,
                        clockDialSelectedContentColor = c.onMint, clockDialUnselectedContentColor = c.ink,
                    ),
                )
            },
        )
    }
}

