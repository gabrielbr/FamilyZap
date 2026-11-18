package app.oie.ui.kid

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import app.oie.R
import app.oie.data.AppState
import app.oie.data.Route
import app.oie.logic.Kind
import app.oie.logic.Message
import app.oie.ui.common.Bar
import app.oie.ui.common.Initial
import app.oie.ui.common.OIcon
import app.oie.ui.common.PrimaryButton
import app.oie.ui.common.QuietButton
import app.oie.ui.theme.OTokens
import app.oie.ui.theme.OieTheme
import app.oie.ui.theme.OieType
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.delay

/** The 5-second undo before a message leaves the tablet (owner, 2026-11-18). */
private const val UNDO_MS = 5_000L

/** The Oiê sticker set (01 Brand/stickers), by word. */
val STICKERS = listOf("Oiê!", "Saudade", "Boa noite", "Te amo", "Tô chegando", "Obrigado")

/** Prototype screens 03, 16–20. Flows: kid-sends-message.md, kid-receives-messages.md. */
@Composable
fun Conversation(state: AppState, contactId: String) {
    val c = OieTheme.colors
    val contact = state.allowed.firstOrNull { it.id == contactId } ?: return
    val canSend = state.canSend(contact) && state.link.connected
    var draft by remember { mutableStateOf(state.drafts[contactId] ?: "") }
    var pending by remember { mutableStateOf<Message?>(null) }
    var stickers by remember { mutableStateOf(false) }
    var recordingSince by remember { mutableLongStateOf(0L) }
    val visible = state.visible(contactId)
    LaunchedEffect(contactId, visible.size) { state.lastSeen[contactId] = System.currentTimeMillis() }

    fun queue(m: Message) { pending = m }
    LaunchedEffect(pending) {
        val m = pending ?: return@LaunchedEffect
        delay(UNDO_MS)
        state.messages += m
        state.link.send(m)
        pending = null
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Bar {
                QuietButton(null, { state.drafts[contactId] = draft; state.route = Route.KidHome }, icon = R.drawable.gg_ic_back, contentDescription = "Voltar")
                Initial(contact.name, OTokens.Size.avatar)
                Text(contact.name, style = OieType.title, color = c.ink)
            }
            val list = rememberLazyListState()
            LazyColumn(
                Modifier.weight(1f).fillMaxWidth().padding(horizontal = OTokens.Spacing.space16),
                state = list, verticalArrangement = Arrangement.spacedBy(OTokens.Spacing.space3),
            ) {
                item { Box(Modifier.height(OTokens.Spacing.space6)) }
                if (visible.isEmpty() && pending == null) item { Text("Mande o primeiro oi para ${contact.name}.", style = OieType.body, color = c.muted) }
                items(visible, key = { it.id }) { m -> Bubble(m, contact.name) }
                pending?.let { p -> item(key = "pending") { Bubble(p, contact.name, status = "Enviando…") } }
                visible.lastOrNull { it.fromKid }?.takeIf { pending == null && visible.last().fromKid }?.let { last ->
                    item { Text("Enviada · ${hour(last.at)}", style = OieType.caption, color = c.muted, modifier = Modifier.fillMaxWidth().padding(start = OTokens.Spacing.space16)) }
                }
            }
            Box(Modifier.fillMaxWidth().height(OTokens.Size.hairline).background(c.line))
            Row(
                Modifier.fillMaxWidth().padding(horizontal = OTokens.Spacing.space16, vertical = OTokens.Spacing.space4),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space3),
            ) {
                if (!canSend) {
                    val q = state.quietNow()
                    Text(
                        if (!state.link.connected) "Peça para ${state.parentName} conectar de novo." else "Horário de silêncio até ${q?.end?.hour}h.",
                        style = OieType.body, color = c.muted,
                    )
                } else if (recordingSince > 0) {
                    Text("Gravando…", style = OieType.label, color = c.ink, modifier = Modifier.weight(1f))
                    Text("Solte para enviar", style = OieType.bodySmall, color = c.muted)
                } else {
                    QuietButton(null, { stickers = true }, icon = R.drawable.gg_ic_sticker, contentDescription = "Figurinhas")
                    OutlinedTextField(
                        draft, { draft = it; state.drafts[contactId] = it }, Modifier.weight(1f),
                        placeholder = { Text("Escreva para ${contact.name}", style = OieType.body, color = c.muted) },
                        textStyle = OieType.body,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = c.ink, unfocusedTextColor = c.ink, focusedContainerColor = c.surface,
                            unfocusedContainerColor = c.surface, focusedBorderColor = c.ink, unfocusedBorderColor = c.line, cursorColor = c.ink,
                        ),
                    )
                    if (draft.isNotBlank()) {
                        PrimaryButton("Enviar", {
                            queue(Message("k${System.nanoTime()}", contactId, true, System.currentTimeMillis(), Kind.TEXT, draft.trim()))
                            draft = ""; state.drafts.remove(contactId)
                        }, icon = R.drawable.gg_ic_send)
                    }
                }
                if (canSend && draft.isBlank()) RecordButton(
                    recording = recordingSince > 0,
                    onStart = { recordingSince = System.currentTimeMillis() },
                    onStop = { send ->
                        val secs = ((System.currentTimeMillis() - recordingSince) / 1000).toInt()
                        recordingSince = 0
                        // Under 1 s is a slip, not a message (flow branch 3a).
                        if (send && secs >= 1) queue(Message("k${System.nanoTime()}", contactId, true, System.currentTimeMillis(), Kind.VOICE, durationSec = secs))
                    },
                )
            }
        }
        pending?.let {
            Row(
                Modifier.align(Alignment.BottomCenter).padding(bottom = OTokens.Size.record + OTokens.Spacing.space12)
                    .clip(RoundedCornerShape(OTokens.Radius.radiusS)).background(c.snackbar)
                    .padding(start = OTokens.Spacing.space6, end = OTokens.Spacing.space2),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space6),
            ) {
                Text("Enviando para ${contact.name}", style = OieType.body, color = c.page)
                Box(
                    Modifier.height(OTokens.Spacing.space12).clickable(role = Role.Button) {
                        // Undo puts the content back, so nothing is lost.
                        if (it.kind == Kind.TEXT) draft = it.text
                        pending = null
                    }.padding(horizontal = OTokens.Spacing.space3),
                    contentAlignment = Alignment.Center,
                ) { Text("Desfazer", style = OieType.label, color = c.page) }
            }
        }
        if (stickers) StickerSheet(onPick = { w ->
            stickers = false
            queue(Message("k${System.nanoTime()}", contactId, true, System.currentTimeMillis(), Kind.STICKER, w))
        }, onClose = { stickers = false })
    }
}

/** Hold to record, release to send; tap to start and tap again to send for anyone who can't hold (WCAG 2.5.7). */
@Composable
private fun RecordButton(recording: Boolean, onStart: () -> Unit, onStop: (send: Boolean) -> Unit) {
    val c = OieTheme.colors
    Box(
        Modifier.size(OTokens.Size.record).scale(if (recording) 0.97f else 1f).clip(CircleShape).background(c.mint)
            .semantics {
                contentDescription = if (recording) "Gravando. Toque para enviar" else "Gravar áudio"
                onClick { if (recording) onStop(true) else onStart(); true }
            }
            .pointerInput(recording) {
                detectTapGestures(
                    onPress = {
                        if (recording) { onStop(true); return@detectTapGestures }
                        onStart()
                        val released = tryAwaitRelease()
                        onStop(released)
                    },
                )
            },
        contentAlignment = Alignment.Center,
    ) { OIcon(R.drawable.gg_ic_mic, tint = c.onMint, size = OTokens.Spacing.space8) }
}

@Composable
private fun Bubble(m: Message, name: String, status: String? = null) {
    val c = OieTheme.colors
    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (m.fromKid) Arrangement.End else Arrangement.Start) {
        Row(
            Modifier.widthIn(max = OTokens.Size.contentMax)
                .clip(RoundedCornerShape(OTokens.Radius.radiusM))
                .background(if (m.fromKid) c.selected else c.surface)
                .then(if (m.fromKid) Modifier else Modifier.border(BorderStroke(OTokens.Size.hairline, c.line), RoundedCornerShape(OTokens.Radius.radiusM)))
                .padding(horizontal = OTokens.Spacing.space4, vertical = OTokens.Spacing.space3),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space3),
        ) {
            val ink = if (m.fromKid) c.onSelected else c.ink
            when (m.kind) {
                Kind.VOICE -> { OIcon(R.drawable.gg_ic_play, tint = ink, contentDescription = "Ouvir áudio de ${m.durationSec} segundos"); Text("0:%02d".format(m.durationSec), style = OieType.caption, color = c.muted) }
                Kind.STICKER -> Text(m.text, style = OieType.title, color = ink)
                Kind.UNSUPPORTED -> Text("$name mandou algo que só abre no WhatsApp.", style = OieType.body, color = c.muted)
                else -> Text(m.text, style = OieType.body, color = ink)
            }
            Text(status ?: hour(m.at), style = OieType.caption, color = c.muted)
        }
    }
}

@Composable
private fun StickerSheet(onPick: (String) -> Unit, onClose: () -> Unit) {
    val c = OieTheme.colors
    Box(Modifier.fillMaxSize().background(c.ink.copy(alpha = 0.45f)).clickable(onClick = onClose), contentAlignment = Alignment.BottomCenter) {
        Column(
            Modifier.widthIn(max = OTokens.Size.contentMax + OTokens.Spacing.space16 * 4)
                .clip(RoundedCornerShape(topStart = OTokens.Radius.radiusL, topEnd = OTokens.Radius.radiusL))
                .background(c.surface).clickable(enabled = false) {}.padding(OTokens.Spacing.space8),
            verticalArrangement = Arrangement.spacedBy(OTokens.Spacing.space4),
        ) {
            Text("Figurinhas", style = OieType.title, color = c.ink)
            Row(horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space4)) {
                STICKERS.forEach { w ->
                    Box(
                        Modifier.size(OTokens.Size.contactTile + OTokens.Spacing.space6).clip(RoundedCornerShape(OTokens.Radius.radiusL))
                            .background(c.page).clickable(role = Role.Button, onClickLabel = "Mandar figurinha $w") { onPick(w) },
                        contentAlignment = Alignment.Center,
                    ) { Text(w, style = OieType.label, color = c.ink) }
                }
            }
        }
    }
}

private fun hour(at: Long): String {
    val t = Instant.ofEpochMilli(at).atZone(ZoneId.systemDefault()).toLocalTime()
    return "%dh%02d".format(t.hour, t.minute)
}
