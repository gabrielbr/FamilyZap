package app.oie.ui.parent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.platform.LocalContext
import app.oie.R
import app.oie.data.AppState
import app.oie.data.LinkState
import app.oie.logic.Contact
import app.oie.logic.ContactSync
import app.oie.ui.common.ICON
import app.oie.ui.common.QuietButton
import app.oie.ui.common.TextLink
import app.oie.ui.text.str
import app.oie.ui.theme.OTokens
import app.oie.ui.theme.OieTheme
import app.oie.ui.theme.OieType
import kotlinx.coroutines.delay

/**
 * Where the parent's contacts are after linking (docs/flows/parent-setup-link.md, v0.17). One
 * wait, timed to the moment a minute has passed since linking, re-reads the clock so "slow" can
 * show; everything else changes when the contacts or the link change.
 */
@Composable
fun contactSyncState(state: AppState): ContactSync.State {
    val ls = state.linkState
    var tick by remember { mutableLongStateOf(0L) }
    val now = remember(tick, ls.linkedAt, ls.contactsSynced) { state.clock() }
    val people = state.wa.contacts().count { !it.isGroup }
    LaunchedEffect(ls.linkedAt) {
        val wait = ls.linkedAt + ContactSync.SLOW_AFTER_MS - state.clock()
        if (ls.linkedAt > 0 && wait > 0) { delay(wait); tick++ }
    }
    return ContactSync.state(ls.status == LinkState.Status.Connected, ls.contactsSynced, people, ls.linkedAt, now)
}

/** One line: bringing them (with the count so far), slow, or none found. Nothing when they're all here. */
@Composable
fun ContactSyncLine(state: AppState, sync: ContactSync.State) {
    val c = OieTheme.colors
    val tx = str
    val people = state.wa.contacts().count { !it.isGroup }
    val text = when (sync) {
        ContactSync.State.SYNCING -> tx.contactsSyncing(people)
        ContactSync.State.SLOW -> tx.contactsSlow
        ContactSync.State.EMPTY -> tx.contactsEmpty
        else -> return
    }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space3)) {
        // The spinner only moves when animations are on (AGENTS.md: honour "Remove animations").
        if (sync == ContactSync.State.SYNCING && animationsOn()) {
            CircularProgressIndicator(Modifier.size(ICON), color = c.ink, trackColor = c.line, strokeWidth = OTokens.Size.hairline * 2)
        }
        Text(text, style = OieType.body, color = if (sync == ContactSync.State.SYNCING) c.muted else c.ink, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun animationsOn(): Boolean {
    val context = LocalContext.current
    return remember {
        runCatching { android.provider.Settings.Global.getFloat(context.contentResolver, android.provider.Settings.Global.ANIMATOR_DURATION_SCALE, 1f) }.getOrDefault(1f) != 0f
    }
}

/**
 * Someone missing from the list, added by name and number once WhatsApp confirms the number
 * (v0.12; shared by setup and Contatos since v0.17). Folded behind one link, except when the
 * list is slow or empty, where it's the way forward.
 */
@Composable
fun AddByNumber(open: Boolean, touched: () -> Unit = {}, onAdd: (Contact) -> Unit) {
    val c = OieTheme.colors
    val tx = str
    val real = LocalRealLink.current
    var shown by remember(open) { mutableStateOf(open) }
    var name by remember { mutableStateOf("") }
    var number by remember { mutableStateOf("") }
    var note by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    if (!shown) { TextLink(tx.addByNumber, { shown = true; touched() }); return }
    Text(tx.addByNumber, style = OieType.label, color = c.ink, modifier = Modifier.padding(top = OTokens.Spacing.space6))
    app.oie.ui.setup.Field(tx.contactNameLabel, name, { name = it; note = null; touched() }, "")
    app.oie.ui.setup.Field(tx.numberLabel, number, { number = it; note = null; touched() }, note ?: tx.numberHelp)
    QuietButton(if (busy) tx.looking else tx.add, {
        if (busy || name.isBlank() || number.count { it.isDigit() } < 10) return@QuietButton
        busy = true
        real?.lookUp(number) { id ->
            busy = false
            when {
                id == null -> note = tx.lookUpFailed
                id.isEmpty() -> note = tx.notOnWhatsApp
                else -> { onAdd(Contact(id, name.trim())); name = ""; number = "" }
            }
        }
    }, icon = R.drawable.gg_ic_plus)
}

/** The parent's line under a contact (v0.19): the number grouped for reading, or "Número não disponível". Groups say "Grupo". */
fun contactNumber(tx: app.oie.ui.text.Strings, ct: Contact, fresh: List<Contact>): String {
    if (ct.isGroup) return tx.groupShort
    return app.oie.logic.ContactNames.display(app.oie.logic.ContactNames.phoneOf(app.oie.logic.ContactNames.withNumber(ct, fresh))).ifEmpty { tx.noNumber }
}

/** The number, then "Maria Silva no WhatsApp" on its own line when the kid sees another name (v0.19 audit: one line each fits a phone). */
@Composable
fun ContactLines(ct: Contact, fresh: List<Contact>) {
    val c = OieTheme.colors
    val tx = str
    val ell = androidx.compose.ui.text.style.TextOverflow.Ellipsis
    Text(contactNumber(tx, ct, fresh), style = OieType.caption, color = c.muted, maxLines = 1, overflow = ell)
    if (ct.waName.isNotBlank()) Text(tx.onWhatsApp(ct.waName), style = OieType.caption, color = c.muted, maxLines = 2, overflow = ell)
}

/**
 * One contact in the parent's hands: the name the kid sees (docs/flows/contacts-quiet-hours.md, v0.19). The WhatsApp
 * name and the number are shown under the field, so nothing has to be remembered; blank goes back
 * to the WhatsApp name. Back cancels.
 */
@Composable
fun ContactEditor(ct: Contact, kid: String, fresh: List<Contact>, onSave: (Contact) -> Unit, onCancel: () -> Unit, onRemove: (() -> Unit)? = null, touched: () -> Unit = {}) {
    val c = OieTheme.colors
    val tx = str
    var name by remember(ct.id) { mutableStateOf(ct.name) }
    androidx.activity.compose.BackHandler(onBack = onCancel)
    val wa = app.oie.logic.ContactNames.whatsAppName(ct)
    androidx.compose.foundation.layout.Column(verticalArrangement = Arrangement.spacedBy(OTokens.Spacing.space4)) {
        // Who this is, before the field: the WhatsApp name and the number, each on its own line.
        androidx.compose.foundation.layout.Column {
            Text(tx.changeName, style = OieType.title, color = c.ink)
            if (ct.paused) Text(tx.pausedLine, style = OieType.body, color = c.ink)
            Text(tx.onWhatsApp(wa), style = OieType.body, color = c.ink)
            Text(contactNumber(tx, ct, fresh), style = OieType.body, color = c.muted)
        }
        // Typing counts as a touch, so the parent area doesn't close mid-name (v0.19 audit).
        app.oie.ui.setup.Field(tx.nameKidSees(kid), name, { name = it; touched() }, tx.renameHelp(wa))
        app.oie.ui.setup.ButtonRow {
            app.oie.ui.common.PrimaryButton(tx.save, { onSave(app.oie.logic.ContactNames.rename(ct, name)) })
            QuietButton(tx.cancel, onCancel, Modifier.height(OTokens.Size.button))
        }
        // v0.21: a chat paused from the parent's WhatsApp can be resumed here too.
        if (ct.paused && onRemove != null) QuietButton(tx.resumeChat, { onSave(app.oie.logic.ContactNames.rename(ct, name).copy(paused = false)) }, Modifier.height(OTokens.Size.button))
        // Contatos only (v0.19 audit): removing moved here from each row, so a phone row has room for the number. Undo stays.
        if (onRemove != null) TextLink(tx.removeNamed(ct.name), onRemove, Modifier.padding(top = OTokens.Spacing.space4).offset(x = -OTokens.Spacing.space3))
    }
}

