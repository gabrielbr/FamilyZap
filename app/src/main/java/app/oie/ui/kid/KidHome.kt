package app.oie.ui.kid

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import app.oie.R
import app.oie.data.AppState
import app.oie.data.Route
import app.oie.ui.common.Banner
import app.oie.ui.common.Bar
import app.oie.ui.common.Initial
import app.oie.ui.common.PrimaryButton
import app.oie.ui.common.QuietButton
import app.oie.ui.setup.Wordmark
import app.oie.ui.theme.OTokens
import app.oie.ui.theme.OieTheme
import app.oie.ui.theme.OieType

/** Prototype screens 01, 02, 04, 21. Flow: docs/flows/kid-sends-message.md. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun KidHome(state: AppState) {
    val c = OieTheme.colors
    val quiet = state.quietNow()
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Bar {
                Wordmark()
                Box(Modifier.weight(1f))
                QuietButton("Pais", { state.route = Route.ParentPin }, icon = R.drawable.gg_ic_gear)
            }
            when {
                !state.link.connected -> Banner(R.drawable.gg_ic_link, "O Oiê se desconectou. Peça para ${state.parentName} conectar de novo.")
                quiet != null -> Banner(R.drawable.gg_ic_clock, "Horário de silêncio até ${quiet.end.hour}h. Dá para ler e ouvir, mas não para mandar.")
            }
            Column(
                Modifier.fillMaxWidth().padding(horizontal = OTokens.Spacing.space16, vertical = OTokens.Spacing.space10),
                verticalArrangement = Arrangement.spacedBy(OTokens.Spacing.space2),
            ) {
                Text("Oiê, ${state.kidName}", style = OieType.display, color = c.ink)
                if (state.allowed.isEmpty()) {
                    // Empty: the kid can't fix it, so no button; the line says who can.
                    Text("Ainda não tem ninguém aqui. Peça para ${state.parentName} escolher quem você pode chamar.", style = OieType.body, color = c.muted)
                } else {
                    Text("Com quem você quer falar?", style = OieType.body, color = c.muted)
                    FlowRow(
                        Modifier.padding(top = OTokens.Spacing.space8),
                        horizontalArrangement = Arrangement.spacedBy(OTokens.Spacing.space8),
                        verticalArrangement = Arrangement.spacedBy(OTokens.Spacing.space6),
                    ) {
                        state.allowed.forEach { ct ->
                            val unread = state.unread(ct.id)
                            val label = ct.name + if (unread > 0) ", $unread ${if (unread == 1) "nova" else "novas"}" else ""
                            Column(
                                Modifier.width(OTokens.Size.contactTile + OTokens.Spacing.space12)
                                    .clip(RoundedCornerShape(OTokens.Radius.radiusM))
                                    .clickable(role = Role.Button) { state.route = Route.Conversation(ct.id) }
                                    .semantics(mergeDescendants = true) { contentDescription = label }
                                    .padding(vertical = OTokens.Spacing.space4),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(OTokens.Spacing.space3),
                            ) {
                                Initial(ct.name, OTokens.Size.contactTile)
                                Text(ct.name, style = OieType.label, color = c.ink)
                                if (unread > 0) Text(
                                    "$unread ${if (unread == 1) "nova" else "novas"}", style = OieType.caption, color = c.page,
                                    modifier = Modifier.clip(RoundedCornerShape(OTokens.Radius.radiusL)).background(c.ink)
                                        .padding(horizontal = OTokens.Spacing.space2, vertical = OTokens.Spacing.space1 / 2),
                                )
                            }
                        }
                    }
                }
            }
        }
        if (!state.kidNoticeSeen && state.allowed.isNotEmpty()) KidNotice(state)
    }
}

/** Prototype screen 04: said once, plainly, never a threat (01 Brand/voice.md). */
@Composable
private fun KidNotice(state: AppState) {
    val c = OieTheme.colors
    Box(Modifier.fillMaxSize().background(c.ink.copy(alpha = 0.45f)).clickable(enabled = false) {}, contentAlignment = Alignment.BottomCenter) {
        Column(
            Modifier.width(OTokens.Size.contentMax)
                .clip(RoundedCornerShape(topStart = OTokens.Radius.radiusL, topEnd = OTokens.Radius.radiusL))
                .background(c.surface).padding(OTokens.Spacing.space8),
            verticalArrangement = Arrangement.spacedBy(OTokens.Spacing.space4),
        ) {
            Text("Antes de começar", style = OieType.title, color = c.ink)
            Text("Suas mensagens saem do WhatsApp de ${state.parentName}, então ${state.parentName} pode ver o que você manda.", style = OieType.body, color = c.ink)
            PrimaryButton("Entendi", { state.kidNoticeSeen = true }, Modifier.fillMaxWidth())
        }
    }
}
